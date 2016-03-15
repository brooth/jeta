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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.squareup.javapoet.*;
import org.brooth.jeta.Factory;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingContext;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.inject.*;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaInjectProcessor extends AbstractProcessor {

    private final TypeName metaScopeTypeName = ParameterizedTypeName.get(
            ClassName.get(MetaScope.class), WildcardTypeName.subtypeOf(TypeName.OBJECT));

    @Nullable
    private String providerAlias;
    private Set<? extends Element> scopes;
    private Multimap<TypeElement, String> scopeEntities;

    private static class StatementSpec {
        String format;
        Object[] args;
        TypeSpec factory;

        public StatementSpec(String format, Object... args) {
            this.format = format;
            this.args = args;
        }
    }

    public MetaInjectProcessor() {
        super(Meta.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        providerAlias = processingContext.processingProperties().getProperty("meta.provider.alias", null);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (context.round() == 1) {
            if (scopes == null) {
                scopes = context.roundEnv().getElementsAnnotatedWith(Scope.class);
                if (scopes.isEmpty())
                    throw new ProcessingException("No meta scope found. You need to define one and pot @Scope annotation on it.");
            }
            return true;
        }

        // check all scopes have processed
        Elements elementUtils = processingContext.processingEnv().getElementUtils();
        for (Element scopeElement : scopes) {
            String scopeName = ((TypeElement) scopeElement).getQualifiedName().toString();
            String extTypeMetacodeStr = MetacodeUtils.getMetacodeOf(elementUtils, scopeName);
            TypeElement metaScopeTypeElement = elementUtils.getTypeElement(extTypeMetacodeStr);
            if (metaScopeTypeElement == null) {
                processingContext.logger().debug("scope '" + scopeName + "' is being processed. skip round");
                return true;
            }
        }

        if (scopeEntities == null) {
            collectScopesEntities();
        }

        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(InjectMetacode.class), masterClassName));

        for (Boolean staticMeta = true; staticMeta != null; staticMeta = staticMeta ? false : null) {
            MethodSpec.Builder methodBuilder;
            if (staticMeta) {
                methodBuilder = MethodSpec.methodBuilder("applyStaticMeta")
                        .addParameter(metaScopeTypeName, "scope", Modifier.FINAL);

            } else {
                methodBuilder = MethodSpec.methodBuilder("applyMeta")
                        .addParameter(metaScopeTypeName, "scope", Modifier.FINAL)
                        .addParameter(masterClassName, "master", Modifier.FINAL);
            }
            methodBuilder.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(void.class);

            for (Element e : scopes) {
                TypeElement scopeElement = (TypeElement) e;
                List<StatementSpec> statements = new ArrayList<>();
                for (Element element : context.elements()) {
                    String elementTypeStr = element.asType().toString();
                    String fieldNameStr = element.getSimpleName().toString();
                    String fieldStatement = null;

                    if (staticMeta) {
                        if (element.getModifiers().contains(Modifier.STATIC))
                            fieldStatement = String.format("%1$s.%2$s = ", elementTypeStr, fieldNameStr);

                    } else {
                        if (!element.getModifiers().contains(Modifier.STATIC))
                            fieldStatement = String.format("master.%1$s = ", fieldNameStr);
                    }
                    if (fieldStatement == null)
                        continue;

                    StatementSpec statement = getResultStatement(scopeElement, element.asType(), fieldStatement, "getInstance()");
                    if (statement != null)
                        statements.add(statement);
                }

                if (!statements.isEmpty()) {
                    ClassName scopeMetacodeClassName = ClassName.get(processingContext.processingEnv().getElementUtils()
                                    .getPackageOf(scopeElement).getQualifiedName().toString(),
                            scopeElement.getSimpleName().toString());
                    ClassName metaScopeClassName = ClassName.get(scopeMetacodeClassName.packageName(),
                            scopeElement.getSimpleName().toString() + "_Metacode.MetaScope");

                    methodBuilder
                            .beginControlFlow("if(scope.isAssignable($T.class))", ClassName.get(scopeElement))
                            .addStatement("final $T s = ($T) scope", metaScopeClassName, metaScopeClassName);

                    for (StatementSpec statement : statements) {
                        methodBuilder.addStatement(statement.format, statement.args);
                        if (statement.factory != null)
                            builder.addType(statement.factory);
                    }

                    methodBuilder.endControlFlow();
                }
            }
            builder.addMethod(methodBuilder.build());
        }

        return false;
    }

    @Nullable
    private StatementSpec getResultStatement(TypeElement scopeElement, TypeMirror returnTypeMirror, String statementPrefix, String getInstanceStr) {
        ProcessingEnvironment env = processingContext.processingEnv();
        String returnTypeStr = env.getTypeUtils().erasure(returnTypeMirror).toString();

        if (providerAlias != null && returnTypeStr.equals(providerAlias))
            returnTypeStr = "org.brooth.jeta.Provider";

        if (returnTypeStr.equals("org.brooth.jeta.Provider")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            if (!scopeEntities.get(scopeElement).contains(returnTypeStr))
                return null;

            StatementSpec statement = getAssignmentStatement(scopeElement, returnTypeStr, "return ", getInstanceStr);
            TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(TypeName.get(returnTypeMirror))
                    .addMethod(MethodSpec.methodBuilder("get")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ClassName.bestGuess(returnTypeStr))
                            .addStatement(statement.format, statement.args)
                            .build())
                    .build();
            return new StatementSpec(statementPrefix + " $L", providerTypeSpec);
        }

        if (returnTypeStr.equals("org.brooth.jeta.Lazy")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            if (!scopeEntities.get(scopeElement).contains(returnTypeStr))
                return null;

            ClassName returnClassName = ClassName.bestGuess(returnTypeStr);
            StatementSpec statement = getAssignmentStatement(scopeElement, returnTypeStr, "instance = ", getInstanceStr);
            TypeSpec lazyTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(TypeName.get(returnTypeMirror))
                    .addField(returnClassName, "instance", Modifier.PRIVATE)
                    .addMethod(MethodSpec.methodBuilder("get")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(returnClassName)
                            .beginControlFlow("if(instance == null)")
                            .beginControlFlow("synchronized (this)")
                            .beginControlFlow("if(instance == null)")
                            .addStatement(statement.format, statement.args)
                            .endControlFlow()
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return instance")
                            .build())
                    .build();
            return new StatementSpec(statementPrefix + " $L", lazyTypeSpec);
        }

        if (returnTypeStr.equals("java.lang.Class")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            if (!scopeEntities.get(scopeElement).contains(returnTypeStr))
                return null;

            return getAssignmentStatement(scopeElement, returnTypeStr, statementPrefix, "getEntityClass()");
        }

        TypeElement typeElement = env.getElementUtils().getTypeElement(returnTypeStr);
        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            if (typeElement.getKind() != ElementKind.INTERFACE)
                throw new IllegalStateException(returnTypeStr + " not allowed. Only interfaces can be used as a meta factory.");
            return getFactoryStatement(scopeElement, typeElement, statementPrefix);
        }

        if (!scopeEntities.get(scopeElement).contains(returnTypeStr))
            return null;
        return getAssignmentStatement(scopeElement, returnTypeStr, statementPrefix, getInstanceStr);
    }

    private StatementSpec getAssignmentStatement(TypeElement scopeElement, String elementTypeStr, String statementPrefix, String getInstanceStr) {
        return new StatementSpec(statementPrefix + "s.$L_$L_MetaEntity().$L",
                elementTypeStr.replaceAll("\\.", "_"), scopeElement.getSimpleName().toString(), getInstanceStr);
    }

    private int factoryIndex = 0;

    private StatementSpec getFactoryStatement(TypeElement scopeElement, TypeElement element, String statementPrefix) {
        String name = element.getSimpleName().toString() + "Impl" + factoryIndex++;

        TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()));

        StatementSpec statement = new StatementSpec(statementPrefix + "new $L(s)", name);
        for (Element subElement : element.getEnclosedElements())
            if (subElement.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) subElement;
                // name -> type
                List<String> paramNames = new ArrayList<>();
                Map<String, TypeMirror> params = new LinkedHashMap<String, TypeMirror>();
                for (VariableElement param : method.getParameters()) {
                    String paramName = param.getSimpleName().toString();
                    paramNames.add(paramName);
                    params.put(paramName, param.asType());
                }

                TypeMirror methodReturnType = method.getReturnType();
                StatementSpec subStatement = getResultStatement(scopeElement, methodReturnType, "return ",
                        "getInstance(" + Joiner.on(',').join(paramNames) + ")");
                if (subStatement == null)
                    return null;

                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(methodReturnType));

                for (String paramName : params.keySet()) {
                    methodSpec.addParameter(TypeName.get(params.get(paramName)), paramName, Modifier.FINAL);
                }

                methodSpec.addStatement(subStatement.format, subStatement.args);
                factoryBuilder.addMethod(methodSpec.build());
            }

        ClassName metaScopeClassName = ClassName.get(processingContext.processingEnv().getElementUtils()
                        .getPackageOf(scopeElement).getQualifiedName().toString(),
                scopeElement.getSimpleName().toString() + "_Metacode.MetaScope");

        statement.factory = factoryBuilder
                .addField(metaScopeClassName, "s", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(metaScopeClassName, "s")
                        .addStatement("this.s = s")
                        .build())
                .build();
        return statement;
    }

    private void collectScopesEntities() {
        scopeEntities = HashMultimap.create();
        for (final Element e : scopes) {
            TypeElement scopeElement = (TypeElement) e;
            Elements elementUtils = processingContext.processingEnv().getElementUtils();
            String metaScopeStr = MetacodeUtils.getMetacodeOf(elementUtils, scopeElement.getQualifiedName().toString());
            TypeElement metaScopeElement = elementUtils.getTypeElement(metaScopeStr);

            final MetaScopeConfig config = metaScopeElement.getAnnotation(MetaScopeConfig.class);
            if (config == null)
                throw new ProcessingException(scopeElement.getSimpleName() + " is not a meta scope. Put @Scope on it.");

            List<String> scopeElements = MetacodeUtils.extractClassesNames(new Runnable() {
                public void run() {
                    config.entities();
                }
            });
            scopeEntities.putAll(scopeElement, scopeElements);
        }
    }

    private String getGenericType(String type) {
        return getGenericType(type, true);
    }

    private String getGenericType(String type, boolean unwrapClass) {
        String genericType;
        int index = type.indexOf('<');
        if (index == -1)
            throw new IllegalStateException(type + " not valid. Specify generic type.");

        genericType = type.substring(index + 1, type.lastIndexOf('>'));
        if (genericType.startsWith("? extends "))
            genericType = genericType.replace("? extends ", "");

        String toValidate = unwrapClass && genericType.startsWith("java.lang.Class") ?
                getGenericType(genericType, false) : genericType;
        if (!toValidate.matches("^[a-zA-Z0-9._$]*"))
            throw new IllegalStateException(type + " not valid meta structure of generics.");

        return genericType;
    }

    @Override
    public Set<Class<? extends Annotation>> collectElementsAnnotatedWith() {
        String metaAlias = processingContext.processingProperties().getProperty("meta.alias", "");
        if (!metaAlias.isEmpty()) {
            try {
                Class<?> aliasClass = Class.forName(metaAlias);
                if (aliasClass.isAssignableFrom(Annotation.class))
                    throw new IllegalArgumentException(metaAlias + " is not a annotation type.");

                HashSet<Class<? extends Annotation>> set = new HashSet<Class<? extends Annotation>>();
                set.add(annotation);
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> aliasAnnotation = (Class<? extends Annotation>) aliasClass;
                set.add(aliasAnnotation);
                return set;

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load meta alias annotation " + metaAlias, e);
            }
        }

        return super.collectElementsAnnotatedWith();
    }
}
