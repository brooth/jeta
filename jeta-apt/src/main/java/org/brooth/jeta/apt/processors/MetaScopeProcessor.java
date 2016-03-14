/*
 * Copyright 2016 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.brooth.jeta.apt.processors;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import org.brooth.jeta.Constructor;
import org.brooth.jeta.Provider;
import org.brooth.jeta.apt.*;
import org.brooth.jeta.inject.*;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaScopeProcessor extends AbstractProcessor {

    @Nullable
    private String defaultScopeStr;

    private static final String assignableStatement = "return scopeClass == $T.class";
    private static final String assignableExtStatement = assignableStatement + " || super.isAssignable(scopeClass)";

    private Set<? extends Element> allMetaEntities;

    public MetaScopeProcessor() {
        super(Scope.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        defaultScopeStr = processingContext.processingProperties().getProperty("meta.scope.default", null);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ProcessingEnvironment env = processingContext.processingEnv();
        TypeElement masterElement = (TypeElement) context.elements().iterator().next();
        Scope annotation = masterElement.getAnnotation(Scope.class);

        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(MetaScopeMetacode.class), masterClassName));

        String masterSimpleNameStr = masterElement.getSimpleName().toString();
        String metaScopeSimpleNameStr = "MetaScope";

        builder.addMethod(MethodSpec.methodBuilder("getMetaScope")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(MetaScope.class), masterClassName))
                .addParameter(masterClassName, "scope")
                .addStatement("return new $L(scope)", metaScopeSimpleNameStr)
                .build());

        final String scopeClassStr = context.metacodeContext().masterElement().getQualifiedName().toString();
        final boolean isDefaultScope = defaultScopeStr != null && defaultScopeStr.equals(scopeClassStr);

        if (allMetaEntities == null) {
            allMetaEntities = context.roundEnv().getElementsAnnotatedWith(MetaEntity.class);
        }

        Set<? extends Element> scopeEntities = Sets.filter(allMetaEntities, new Predicate<Element>() {
            public boolean apply(Element input) {
                final MetaEntity a = input.getAnnotation(MetaEntity.class);
                String scope = MetacodeUtils.extractClassName(new Runnable() {
                    public void run() {
                        a.scope();
                    }
                });

                if (scopeClassStr.equals(scope))
                    return true;

                if (scope.equals(Void.class.getCanonicalName())) {
                    if (defaultScopeStr == null)
                        throw new ProcessingException(input.getSimpleName().toString() + " has undefined scope. " +
                                "You need to set the scope to @MetaEntity(scope) or define default one as 'meta.scope.default' property");
                    if (isDefaultScope)
                        return true;
                }

                return false;
            }
        });

        if (scopeEntities.isEmpty()) {
            processingContext.logger().warn("Scope '" + scopeClassStr + "' has no entities.");
            return false;
        }

        TypeVariableName sTypeVariableName = TypeVariableName.get("S", masterClassName);
        TypeName metaScopeTypeName = ParameterizedTypeName.get(ClassName.get(MetaScope.class), sTypeVariableName);

        TypeSpec.Builder metaScopeTypeSpecBuilder = TypeSpec.classBuilder(metaScopeSimpleNameStr)
                .addTypeVariable(sTypeVariableName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addField(masterClassName, "scope", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.methodBuilder("getScope")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return (S) scope")
                        .returns(sTypeVariableName)
                        .build());

        MethodSpec.Builder metaScopeConstructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(masterClassName, "scope");

        MethodSpec.Builder assignableMethodBuilder = MethodSpec.methodBuilder("isAssignable")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(Class.class), "scopeClass")
                .returns(boolean.class);

        TypeElement assignableFrom = getAssignableFrom(masterElement);
        if (assignableFrom == masterElement) {
            metaScopeTypeSpecBuilder.addSuperinterface(metaScopeTypeName);
        } else {
            metaScopeConstructorBuilder.addStatement("super(scope)");
            ClassName assignableFromClassName = ClassName.get(assignableFrom);
            metaScopeTypeSpecBuilder.superclass(ParameterizedTypeName.get(ClassName.get(assignableFromClassName.packageName(),
                    assignableFromClassName.simpleName() + JetaProcessor.METACODE_CLASS_POSTFIX + ".MetaScope"), sTypeVariableName));
        }
        assignableMethodBuilder.addStatement(assignableFrom == masterElement ? assignableStatement : assignableExtStatement,
                TypeName.get(context.metacodeContext().masterElement().asType()));
        metaScopeTypeSpecBuilder.addMethod(assignableMethodBuilder.build());

        metaScopeConstructorBuilder.addStatement("this.scope = scope");

        String masterPackageStr = env.getElementUtils().getPackageOf(masterElement).getQualifiedName().toString();
        int providerFieldIndex = 0;
        List<ClassName> ofTypes = new ArrayList<ClassName>(scopeEntities.size());
        for (Element entityElement : scopeEntities) {
            MetaEntity metaEntityAnnotation = entityElement.getAnnotation(MetaEntity.class);
            String metaEntityClassStr = ((TypeElement) entityElement).getQualifiedName().toString();

            String ofTypeStr = getOfClass(metaEntityAnnotation);
            if (ofTypeStr.equals(Void.class.getCanonicalName()))
                ofTypeStr = metaEntityClassStr;

            ClassName ofClassName = ClassName.bestGuess(ofTypeStr);
            ofTypes.add(ofClassName);

            String metaEntityNameStr = ofClassName.packageName().replace('.', '_') + '_' +
                    MetacodeUtils.getSimpleMetaNodeOf(env.getElementUtils(), ofTypeStr, "_MetaEntity");
            ClassName metaEntityClassName = ClassName.get(masterPackageStr, masterElement.getSimpleName() + "_Metacode."
                    + metaEntityNameStr);

            String providerNameStr = "provider" + providerFieldIndex++;
            ParameterizedTypeName providerTypeName = ParameterizedTypeName.get(ClassName.get(Provider.class), metaEntityClassName);

            ClassName metaEntityProviderMetacode = ClassName.bestGuess(MetacodeUtils.getMetacodeOf(
                    env.getElementUtils(), metaEntityClassStr));

            metaScopeTypeSpecBuilder
                    .addField(FieldSpec.builder(ClassName.OBJECT, providerNameStr + "_MONITOR", Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("new $T()", ClassName.OBJECT)
                            .build())
                    .addField(providerTypeName, providerNameStr, Modifier.PRIVATE)
                    .addMethod(MethodSpec.methodBuilder(metaEntityNameStr)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(metaEntityClassName)
                            .beginControlFlow("if ($L == null)", providerNameStr)
                            .beginControlFlow("synchronized($L_MONITOR)", providerNameStr)
                            .beginControlFlow("if ($L == null)", providerNameStr)
                            .addStatement("$L = new $T.MetaEntityProvider()", providerNameStr, metaEntityProviderMetacode)
                            .endControlFlow()
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return $L.get()", providerNameStr)
                            .build());

            TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(metaEntityNameStr)
                    .addJavadoc("emitted by " + metaEntityClassStr + '\n').addModifiers(Modifier.PUBLIC)
                    .addMethod(MethodSpec.methodBuilder("getEntityClass")
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(ParameterizedTypeName.get(ClassName.get(Class.class),
                                    WildcardTypeName.subtypeOf(ofClassName)))
                            .build());

            // meta entity interface
            String extTypeStr = getExtClass(metaEntityAnnotation);
            if (extTypeStr.equals(Void.class.getCanonicalName()))
                extTypeStr = null;

            if (extTypeStr != null) {
                String extScope = getExtScopeClass(metaEntityAnnotation);
                if (extScope.equals(Void.class.getCanonicalName())) {
                    extScope = getExtClass(annotation);
                    if (extScope.equals(Void.class.getCanonicalName()))
                        throw new ProcessingException("Unknown scope of extending entity '" + extTypeStr +
                                "'. Set the scope via '@MetaEntity(extScope=?) " + entityElement.getSimpleName().toString() +
                                "' or '@Scope(ext=?) " + masterSimpleNameStr + "'");
                }

                ClassName extScopeClassName = ClassName.bestGuess(extScope);
                interfaceBuilder.addSuperinterface(ClassName.get(extScopeClassName.packageName(),
                        extScopeClassName.simpleName() + "_Metacode." + extTypeStr.replace('.', '_') + "_MetaEntity"));
            }

            boolean isSelfProvider = metaEntityClassStr.equals(ofTypeStr);
            List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
            for (Element subElement : ((TypeElement) entityElement).getEnclosedElements()) {
                boolean validInitConstructor = entityElement.getKind() == ElementKind.CLASS
                        && !subElement.getModifiers().contains(Modifier.PRIVATE)
                        && ((isSelfProvider && subElement.getSimpleName().contentEquals("<init>")) ||
                        subElement.getAnnotation(Constructor.class) != null);

                if (validInitConstructor)
                    constructors.add((ExecutableElement) subElement);
            }

            for (ExecutableElement constructor : constructors) {
                List<ParameterSpec> params = new ArrayList<ParameterSpec>();
                params.add(ParameterSpec.builder(ClassName.OBJECT, "__scope__").build());
                for (VariableElement input : constructor.getParameters()) {
                    TypeMirror paramType = input.asType();
                    String paramName = input.getSimpleName().toString();
                    if (!paramName.equals("__scope__")) {
                        params.add(ParameterSpec.builder(TypeName.get(paramType), paramName).build());
                    }
                }

                interfaceBuilder.addMethod(
                        MethodSpec.methodBuilder("getInstance")
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(ofClassName).addParameters(params).build());
            }

            builder.addType(interfaceBuilder.build());
        }

        String[] types = new String[ofTypes.size()];
        Arrays.fill(types, "\n\t$T.class");
        Object[] args = ofTypes.toArray(new ClassName[ofTypes.size()]);
        builder.addAnnotation(AnnotationSpec.builder(MetaScopeConfig.class)
                .addMember("entities", "{" + Joiner.on(',').join(types) + "\n}", args)
                .build());

        metaScopeTypeSpecBuilder.addMethod(metaScopeConstructorBuilder.build());
        builder.addType(metaScopeTypeSpecBuilder.build());
        return false;
    }

    private TypeElement getAssignableFrom(final TypeElement scopeElement) {
        String assignableStr = MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                scopeElement.getAnnotation(Scope.class).assignable();
            }
        });
        if (assignableStr.equals(Void.class.getCanonicalName())) {
            return scopeElement;
        }

        return getAssignableFrom(processingContext.processingEnv().getElementUtils().getTypeElement(assignableStr));
    }

    private String getOfClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.of();
            }
        });
    }

    private String getExtClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.ext();
            }
        });
    }

    private String getExtScopeClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.extScope();
            }
        });
    }

    private String getExtClass(final Scope annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.ext();
            }
        });
    }

    @Override
    public boolean ignoreUpToDate() {
        return true;
    }
}
