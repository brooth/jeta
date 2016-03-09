/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta.apt.processors;

import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import org.brooth.jeta.Factory;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.meta.InjectMetacode;
import org.brooth.jeta.meta.Meta;
import org.brooth.jeta.meta.MetaEntityFactory;
import org.brooth.jeta.meta.Scope;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaProcessor extends AbstractProcessor {

    private final Map<TypeElement, String> factoryElements = new HashMap<TypeElement, String>();

    private final TypeName metaEntityFactoryTypeName = TypeName.get(MetaEntityFactory.class);
    private final ClassName scopeClassName = ClassName.get(Scope.class);

    @Nullable
    private String providerAlias;

    public MetaProcessor() {
        super(Meta.class);
    }

    @Override
    public Set<Class<? extends Annotation>> collectElementsAnnotatedWith() {
        providerAlias = processingContext.processingProperties().getProperty("meta.provider.alias", null);
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

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(InjectMetacode.class), masterClassName));

        factoryElements.clear();
        ClassName metaFactoryClassName = ClassName.get(MetaEntityFactory.class);
        for (Boolean staticMeta = true; staticMeta != null; staticMeta = staticMeta ? false : null) {
            MethodSpec.Builder methodBuilder;
            if (staticMeta) {
                methodBuilder = MethodSpec.methodBuilder("applyStaticMeta")
                        .addParameter(scopeClassName, "scope", Modifier.FINAL)
                        .addParameter(metaFactoryClassName, "factory", Modifier.FINAL);

            } else {
                methodBuilder = MethodSpec.methodBuilder("applyMeta")
                        .addParameter(scopeClassName, "scope", Modifier.FINAL)
                        .addParameter(masterClassName, "master", Modifier.FINAL)
                        .addParameter(metaFactoryClassName, "factory", Modifier.FINAL);
            }

            methodBuilder
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class);

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

                addReturnStatement(methodBuilder, processingContext.processingEnv(), element.asType(),
                        Collections.<String, TypeMirror>emptyMap(), fieldStatement, null);
            }

            builder.addMethod(methodBuilder.build());
        }

        for (TypeElement element : factoryElements.keySet())
            buildFactoryImpl(builder, processingContext.processingEnv(), element, factoryElements.get(element));

        return false;
    }

    private void buildFactoryImpl(TypeSpec.Builder builder, ProcessingEnvironment env, TypeElement element, String name) {
        TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()))
                .addField(scopeClassName, "scope", Modifier.PRIVATE, Modifier.FINAL)
                .addField(metaEntityFactoryTypeName, "factory", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(scopeClassName, "scope")
                        .addParameter(metaEntityFactoryTypeName, "factory")
                        .addStatement("this.scope = scope")
                        .addStatement("this.factory = factory")
                        .build());

        for (Element subElement : element.getEnclosedElements())
            if (subElement.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) subElement;
                // name -> type
                Map<String, TypeMirror> params = new LinkedHashMap<String, TypeMirror>();
                for (VariableElement param : method.getParameters())
                    params.put(param.getSimpleName().toString(), param.asType());

                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(method.getReturnType()));

                for (String paramName : params.keySet()) {
                    methodSpec.addParameter(TypeName.get(params.get(paramName)), paramName, Modifier.FINAL);
                }

                addReturnStatement(methodSpec, env, method.getReturnType(), params, "return ", "return null");
                factoryBuilder.addMethod(methodSpec.build());

            }

        builder.addType(factoryBuilder.build());
    }

    private void addReturnStatement(MethodSpec.Builder methodBuilder, ProcessingEnvironment env,
                                    TypeMirror returnTypeMirror, Map<String, TypeMirror> params,
                                    String statementPrefix, @Nullable String scopeElseStatement) {
        String returnTypeStr = env.getTypeUtils().erasure(returnTypeMirror).toString();
        Collection<String> paramValues = new ArrayList<String>(params.size() + 1);
        paramValues.add("scope");
        paramValues.addAll(params.keySet());
        String getInstanceStr = String.format("getInstance(%s)", Joiner.on(", ").join(paramValues));

        if (providerAlias != null && returnTypeStr.equals(providerAlias)) {
            returnTypeStr = "org.brooth.jeta.Provider";
        }

        if (returnTypeStr.equals("org.brooth.jeta.Provider")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(TypeName.get(returnTypeMirror))
                    .addMethod(MethodSpec.methodBuilder("get")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ClassName.bestGuess(returnTypeStr))
                            .addStatement("return " + getResultStatement(env, returnTypeStr, getInstanceStr))
                            .build())
                    .build();
            addReturnStatement(methodBuilder, returnTypeStr, scopeElseStatement, statementPrefix + " $L", providerTypeSpec);

        } else if (returnTypeStr.equals("org.brooth.jeta.Lazy")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            ClassName returnClassName = ClassName.bestGuess(returnTypeStr);
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
                            .addStatement("instance = " + getResultStatement(env, returnTypeStr, getInstanceStr))
                            .endControlFlow()
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return instance")
                            .build())
                    .build();
            addReturnStatement(methodBuilder, returnTypeStr, scopeElseStatement, statementPrefix + " $L", lazyTypeSpec);

        } else if (returnTypeStr.equals("java.lang.Class")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            addReturnStatement(methodBuilder, returnTypeStr, scopeElseStatement,
                    statementPrefix + getResultStatement(env, returnTypeStr, "getEntityClass()"));

        } else {
            addReturnStatement(methodBuilder, returnTypeStr, scopeElseStatement,
                    statementPrefix + getResultStatement(env, returnTypeMirror.toString(), getInstanceStr));
        }
    }

    private void addReturnStatement(MethodSpec.Builder methodBuilder, String elementTypeStr, @Nullable String scopeElseStatement,
                                    String format, Object... args) {
        TypeElement typeElement = processingContext.processingEnv().getElementUtils().getTypeElement(elementTypeStr);
        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            methodBuilder.beginControlFlow("if(scope.getClass() == $T.class)",
                    ClassName.get(Scope.Default.class));

        } else {
            methodBuilder.beginControlFlow(String.format("if(scope.getClass() == %s.MetaEntity.SCOPE)",
                    MetacodeUtils.getMetacodeOf(processingContext.processingEnv().getElementUtils(), elementTypeStr)));
        }

        methodBuilder.addStatement(format, args);
        methodBuilder.endControlFlow();

        if (scopeElseStatement != null) {
            methodBuilder.beginControlFlow("else")
                    .addStatement(scopeElseStatement)
                    .endControlFlow();
        }
    }

    private String getResultStatement(ProcessingEnvironment env, String elementTypeStr, String getInstanceStr) {
        TypeElement typeElement = env.getElementUtils().getTypeElement(elementTypeStr);
        if (typeElement == null)
            throw new ProcessingException("Element \"" + elementTypeStr + "\" not suitable for meta processing.");

        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            if (typeElement.getKind() != ElementKind.INTERFACE)
                throw new IllegalStateException(elementTypeStr + " only interfaces allowed to be used as a meta factory.");

            if (!factoryElements.containsKey(typeElement))
                factoryElements.put(typeElement, typeElement.getSimpleName().toString() + "Impl" + factoryElements.size());

            return String.format("new %s(scope, factory)", factoryElements.get(typeElement));
        }

        return String.format("((%1$s.MetaEntity)\n\tfactory.getMetaEntity(%2$s.class))\n\t\t\t.%3$s",
                MetacodeUtils.getMetacodeOf(env.getElementUtils(), elementTypeStr),
                elementTypeStr, getInstanceStr);
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
}
