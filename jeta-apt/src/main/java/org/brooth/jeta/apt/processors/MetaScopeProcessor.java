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
import org.brooth.jeta.apt.*;
import org.brooth.jeta.inject.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaScopeProcessor extends AbstractProcessor {

    private final AnnotationSpec suppressWarningsUnchecked = AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", "\"unchecked\"").build();
    @Nullable
    private String defaultScopeStr;

    private static final String assignableStatement = "return scopeClass == $T.class";
    private static final String assignableExtStatement = assignableStatement + " || super.isAssignable(scopeClass)";

    private final ClassName iMetaEntityClassName = ClassName.get(IMetaEntity.class);

    private Set<? extends Element> allMetaEntities;
    private Map<String, List<String>> scopesEntities;

    public MetaScopeProcessor() {
        super(Scope.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        defaultScopeStr = processingContext.processingProperties().getProperty("meta.scope.default", null);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (scopesEntities == null) {
            Set<? extends Element> scopes = context.roundEnv().getElementsAnnotatedWith(Scope.class);
            scopesEntities = new HashMap<String, List<String>>(scopes.size());
            for (Element scopeElement : scopes)
                scopesEntities.put(((TypeElement) scopeElement).getQualifiedName().toString(), new ArrayList<String>());
        }

        ProcessingEnvironment env = processingContext.processingEnv();
        TypeElement masterElement = (TypeElement) context.elements().iterator().next();
        Scope scopeAnnotation = masterElement.getAnnotation(Scope.class);
        String scopeExtClassStr = getExtClass(scopeAnnotation);
        if (!isVoid(scopeExtClassStr)) {
            if (!scopesEntities.containsKey(scopeExtClassStr)) {
                Elements elementUtils = env.getElementUtils();
                String extTypeMetacodeStr = MetacodeUtils.getMetacodeOf(elementUtils, scopeExtClassStr);
                TypeElement extTypeElement = elementUtils.getTypeElement(extTypeMetacodeStr);
                if (extTypeElement == null)
                    throw new ProcessingException("Can't extend '" + scopeExtClassStr + "'. Meta scope not found.");

            } else if (scopesEntities.get(scopeExtClassStr).isEmpty()) {
                processingContext.logger().debug("ext scope '" + scopeExtClassStr + "' not processed yet. skip round");
                return true;
            }
        }

        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(MetaScopeMetacode.class), masterClassName));

        String metaScopeSimpleNameStr = "MetaScopeImpl";

        builder.addMethod(MethodSpec.methodBuilder("getMetaScope")
                .addAnnotation(Override.class)
                .addAnnotation(suppressWarningsUnchecked)
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

        Set<? extends Element> scopeEntities = getScopeEntities(scopeClassStr, isDefaultScope);
        if (scopeEntities.isEmpty()) {
            processingContext.logger().warn("Scope '" + scopeClassStr + "' has no entities.");
            return false;
        }

        TypeVariableName sTypeVariableName = TypeVariableName.get("S", masterClassName);
        TypeName metaScopeTypeName = ParameterizedTypeName.get(ClassName.get(MetaScope.class), sTypeVariableName);
        TypeSpec.Builder metaScopeTypeSpecBuilder = TypeSpec.classBuilder(metaScopeSimpleNameStr)
                .addTypeVariable(sTypeVariableName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addField(sTypeVariableName, "scope", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.methodBuilder("getScope")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return scope")
                        .returns(sTypeVariableName)
                        .build());

        MethodSpec.Builder metaScopeConstructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sTypeVariableName, "scope");

        MethodSpec.Builder assignableMethodBuilder = MethodSpec.methodBuilder("isAssignable")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(Class.class), "scopeClass")
                .returns(boolean.class);

        if (isVoid(scopeExtClassStr)) {
            metaScopeTypeSpecBuilder.addSuperinterface(metaScopeTypeName);
            assignableMethodBuilder.addStatement(assignableStatement,
                    TypeName.get(context.metacodeContext().masterElement().asType()));
        } else {
            metaScopeConstructorBuilder.addStatement("super(scope)");
            ClassName scopeExtClassName = ClassName.bestGuess(scopeExtClassStr);
            metaScopeTypeSpecBuilder.superclass(ParameterizedTypeName.get(ClassName.get(scopeExtClassName.packageName(),
                    scopeExtClassName.simpleName() + JetaProcessor.METACODE_CLASS_POSTFIX + ".MetaScopeImpl"), sTypeVariableName));
            assignableMethodBuilder.addStatement(assignableExtStatement,
                    TypeName.get(context.metacodeContext().masterElement().asType()));
        }
        metaScopeTypeSpecBuilder.addMethod(assignableMethodBuilder.build());
        metaScopeConstructorBuilder.addStatement("this.scope = scope");

        TypeVariableName eTypeVariableName = TypeVariableName.get("E", ClassName.OBJECT);
        MethodSpec.Builder getMetaEntityMethodBuilder = MethodSpec.methodBuilder("getMetaEntity")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(suppressWarningsUnchecked)
                .addTypeVariable(eTypeVariableName)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), eTypeVariableName), "entityClass")
                .returns(ParameterizedTypeName.get(ClassName.get(IMetaEntity.class),
                        WildcardTypeName.subtypeOf(eTypeVariableName)));

        String masterPackageStr = env.getElementUtils().getPackageOf(masterElement).getQualifiedName().toString();
        int entityImplFieldIndex = 0;
        List<ClassName> ofTypes = new ArrayList<ClassName>(scopeEntities.size());
        for (Element entityElement : scopeEntities) {
            TypeElement metaEntityElement = (TypeElement) entityElement;
            MetaEntity metaEntityAnnotation = entityElement.getAnnotation(MetaEntity.class);
            String metaEntityClassStr = metaEntityElement.getQualifiedName().toString();

            String ofTypeStr = getOfClass(metaEntityAnnotation);
            if (isVoid(ofTypeStr))
                ofTypeStr = metaEntityClassStr;

            ClassName ofClassName = ClassName.bestGuess(ofTypeStr);
            ofTypes.add(ofClassName);

            String metaEntityNameStr = ofClassName.packageName().replace('.', '_') + '_' +
                    MetacodeUtils.getSimpleMetaNodeOf(env.getElementUtils(), ofTypeStr, "_MetaEntity");
            ClassName metaEntityClassName = ClassName.get(masterPackageStr,
                    masterElement.getSimpleName() + "_Metacode." + metaEntityNameStr);

            String entityImplNameStr = "entity" + entityImplFieldIndex++;
            ClassName metaEntityProviderMetacode = ClassName.bestGuess(MetacodeUtils.getMetacodeOf(
                    env.getElementUtils(), metaEntityClassStr));
            String metaEntityImplMethodName = ofClassName.packageName().replace('.', '_') + '_' +
                    MetacodeUtils.getSimpleMetaNodeOf(env.getElementUtils(), ofTypeStr,
                            '_' + masterClassName.simpleName() + "_MetaEntity");

            metaScopeTypeSpecBuilder
                    .addField(metaEntityClassName, entityImplNameStr, Modifier.PRIVATE)
                    .addMethod(MethodSpec.methodBuilder(metaEntityImplMethodName)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(metaEntityClassName)
                            .beginControlFlow("if ($L == null)", entityImplNameStr)
                            .beginControlFlow("synchronized($T.class)", metaEntityClassName)
                            .beginControlFlow("if ($L == null)", entityImplNameStr)
                            .addStatement("$L = new $T.MetaEntityImpl(getScope())", entityImplNameStr, metaEntityProviderMetacode)
                            .endControlFlow()
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return $L", entityImplNameStr)
                            .build());

            TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(metaEntityNameStr)
                    .addJavadoc("emitted by " + metaEntityClassStr + '\n').addModifiers(Modifier.PUBLIC);

            String extTypeStr = getExtClass(metaEntityAnnotation);
            if (isVoid(extTypeStr))
                extTypeStr = null;

            if (extTypeStr != null) {
                ClassName extScopeClassName = lookupExtEntityScope(scopeExtClassStr, extTypeStr);
                if (extScopeClassName == null)
                    throw new ProcessingException("Undefined scope of '" + extTypeStr + "' element. Allowed to extents " +
                            "entities from super scopes only");

                ClassName extMetaEntityClassName = ClassName.get(extScopeClassName.packageName(),
                        extScopeClassName.simpleName() + "_Metacode." + extTypeStr.replace('.', '_') + "_MetaEntity");
                interfaceBuilder.addSuperinterface(extMetaEntityClassName);

                String extScopeClassStr = extScopeClassName.packageName().replace('.', '_') + '_' +
                        MetacodeUtils.getSimpleMetaNodeOf(env.getElementUtils(), extTypeStr,
                                '_' + extScopeClassName.simpleName() + "_MetaEntity");

                metaScopeTypeSpecBuilder
                        .addMethod(MethodSpec.methodBuilder(extScopeClassStr)
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(extMetaEntityClassName)
                                .addStatement("return $L()", metaEntityImplMethodName)
                                .build());
            } else {
                interfaceBuilder.addSuperinterface(ParameterizedTypeName.get(iMetaEntityClassName, ofClassName));
            }

            boolean isSelfProvider = metaEntityClassStr.equals(ofTypeStr);
            List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
            for (Element subElement : metaEntityElement.getEnclosedElements()) {
                boolean validInitConstructor = !subElement.getModifiers().contains(Modifier.PRIVATE)
                        && ((isSelfProvider && subElement.getSimpleName().contentEquals("<init>")) ||
                        subElement.getAnnotation(Constructor.class) != null);

                if (validInitConstructor)
                    constructors.add((ExecutableElement) subElement);
            }

            for (ExecutableElement constructor : constructors) {
                List<ParameterSpec> params = new ArrayList<ParameterSpec>();
                for (VariableElement input : constructor.getParameters()) {
                    TypeMirror paramType = input.asType();
                    String paramName = input.getSimpleName().toString();
                    if (!paramName.equals("__scope__")) {
                        params.add(ParameterSpec.builder(TypeName.get(paramType), paramName).build());
                    }
                }

                interfaceBuilder.addMethod(MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ofClassName).addParameters(params).build());
            }

            getMetaEntityMethodBuilder
                    .beginControlFlow("if (entityClass == $T.class)", ofClassName)
                    .addStatement("return (IMetaEntity<? extends E>) $L()", metaEntityImplMethodName)
                    .endControlFlow();

            builder.addType(interfaceBuilder.build());
            scopesEntities.get(masterElement.getQualifiedName().toString()).add(ofTypeStr);
        }

        String[] types = new String[ofTypes.size()];
        Arrays.fill(types, "\n\t$T.class");
        Object[] args = ofTypes.toArray(new ClassName[ofTypes.size()]);
        builder.addAnnotation(AnnotationSpec.builder(MetaScopeConfig.class)
                .addMember("entities", "{" + Joiner.on(',').join(types) + "\n}", args)
                .build());

        metaScopeTypeSpecBuilder
                .addMethod(metaScopeConstructorBuilder.build())
                .addMethod(getMetaEntityMethodBuilder
                        .addStatement("return " + (isVoid(scopeExtClassStr) ? "null" : "super.getMetaEntity(entityClass)"))
                        .build());
        builder.addType(metaScopeTypeSpecBuilder.build());
        return false;
    }

    private Set<? extends Element> getScopeEntities(final String scopeClassStr, final boolean isDefaultScope) {
        return Sets.filter(allMetaEntities, new Predicate<Element>() {
            public boolean apply(Element input) {
                final MetaEntity a = input.getAnnotation(MetaEntity.class);
                String scope = MetacodeUtils.extractClassName(new Runnable() {
                    public void run() {
                        a.scope();
                    }
                });

                if (scopeClassStr.equals(scope))
                    return true;

                if (isVoid(scope)) {
                    if (defaultScopeStr == null)
                        throw new ProcessingException(input.getSimpleName().toString() + " has undefined scope. " +
                                "You need to set the scope to @MetaEntity(scope) or define default one as 'meta.scope.default' property");
                    if (isDefaultScope)
                        return true;
                }

                return false;
            }
        });
    }

    @Nullable
    private ClassName lookupExtEntityScope(@Nullable String extScopeStr, String extTypeStr) {
        if (extScopeStr == null)
            return null;

        List<String> scopeEntities = scopesEntities.get(extScopeStr);
        if (scopeEntities == null) {
            Elements elementUtils = processingContext.processingEnv().getElementUtils();
            String extTypeMetacodeStr = MetacodeUtils.getMetacodeOf(elementUtils, extScopeStr);
            TypeElement extTypeElement = elementUtils.getTypeElement(extTypeMetacodeStr);
            final MetaScopeConfig config = extTypeElement.getAnnotation(MetaScopeConfig.class);
            if (config == null)
                throw new ProcessingException(extTypeMetacodeStr + " is not a scope. Can't extend it.");
            scopeEntities = getScopeEntities(config);
        }
        for (String scopeEntity : scopeEntities)
            if (extTypeStr.equals(scopeEntity))
                return ClassName.bestGuess(extScopeStr);

        Scope extExtScopeAnnotation = processingContext.processingEnv().getElementUtils()
                .getTypeElement(extScopeStr).getAnnotation(Scope.class);
        String extExtScopeStr = getExtClass(extExtScopeAnnotation);
        if (!isVoid(extExtScopeStr))
            return lookupExtEntityScope(extExtScopeStr, extTypeStr);

        return null;
    }

    private boolean isVoid(String str) {
        return str.equals(Void.class.getCanonicalName());
    }

    @Nonnull
    private List<String> getScopeEntities(final MetaScopeConfig config) {
        return MetacodeUtils.extractClassesNames(new Runnable() {
            @Override
            public void run() {
                config.entities();
            }
        });
    }

    @Nonnull
    private String getOfClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.of();
            }
        });
    }

    @Nonnull
    private String getExtClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.ext();
            }
        });
    }

    @Nonnull
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
