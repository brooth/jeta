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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.squareup.javapoet.*;
import org.brooth.jeta.Factory;
import org.brooth.jeta.apt.*;
import org.brooth.jeta.inject.InjectMetacode;
import org.brooth.jeta.inject.Meta;
import org.brooth.jeta.inject.MetaEntityFactory;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaInjectProcessor extends AbstractProcessor {

    private final TypeName metaEntityFactoryTypeName = TypeName.get(MetaEntityFactory.class);
    private final ClassName metaFactoryClassName = ClassName.get(MetaEntityFactory.class);

    @Nullable
    private String providerAlias;

    private static class StatementContext {
        TypeSpec factoryTypeSpec;
        TypeElement scopeElement;
        String format;
        Object[] args;
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
        if (MetaScopeProcessor.scopeEntities == null)
            throw new IllegalStateException("MetaInjectProcessor must follow after MetaScopeProcessor");

        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(InjectMetacode.class), masterClassName));

        for (Boolean staticMeta = true; staticMeta != null; staticMeta = staticMeta ? false : null) {
            Multimap<TypeElement, StatementContext> statementContexts = HashMultimap.create();

            MethodSpec.Builder methodBuilder;
            if (staticMeta) {
                methodBuilder = MethodSpec.methodBuilder("applyStaticMeta")
                        .addParameter(ClassName.OBJECT, "scope", Modifier.FINAL)
                        .addParameter(metaFactoryClassName, "factory", Modifier.FINAL);

            } else {
                methodBuilder = MethodSpec.methodBuilder("applyMeta")
                        .addParameter(ClassName.OBJECT, "scope", Modifier.FINAL)
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

                StatementContext statementContext = addReturnStatement(element.asType(),
                        Collections.<String, TypeMirror>emptyMap(), fieldStatement);

                if (statementContexts.containsKey(statementContext.scopeElement))
                    statementContexts.get(statementContext.scopeElement).add(statementContext);
                else
                    statementContexts.put(statementContext.scopeElement, statementContext);
            }

            for (TypeElement scopeElement : statementContexts.keySet()) {
                methodBuilder.beginControlFlow("if($T.isAssignable(scope))",
                        ClassName.bestGuess(MetacodeUtils.getMetacodeOf(processingContext.processingEnv().getElementUtils(),
                                scopeElement.getQualifiedName().toString())));
                Collection<StatementContext> statements = statementContexts.get(scopeElement);
                for (StatementContext statementContext : statements) {
                    methodBuilder.addStatement(statementContext.format, statementContext.args);
                }
                methodBuilder.endControlFlow();
            }

            for (StatementContext statementContext : statementContexts.values())
                if (statementContext.factoryTypeSpec != null)
                    builder.addType(statementContext.factoryTypeSpec);

            builder.addMethod(methodBuilder.build());
        }

        return false;
    }

    private StatementContext addReturnStatement(TypeMirror returnTypeMirror, Map<String, TypeMirror> params, String statementPrefix) {
        ProcessingEnvironment env = processingContext.processingEnv();
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
            StatementContext getStatement = getResultStatementContext(returnTypeStr, "return ", getInstanceStr);
            TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(TypeName.get(returnTypeMirror))
                    .addMethod(MethodSpec.methodBuilder("get")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ClassName.bestGuess(returnTypeStr))
                            .addStatement(getStatement.format, getStatement.args)
                            .build())
                    .build();
            getStatement.format = statementPrefix + " $L";
            getStatement.args = new Object[]{providerTypeSpec};
            return getStatement;
        }

        if (returnTypeStr.equals("org.brooth.jeta.Lazy")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            ClassName returnClassName = ClassName.bestGuess(returnTypeStr);
            StatementContext getStatement = getResultStatementContext(returnTypeStr, "instance = ", getInstanceStr);
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
                            .addStatement(getStatement.format, getStatement.args)
                            .endControlFlow()
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return instance")
                            .build())
                    .build();
            getStatement.format = statementPrefix + " $L";
            getStatement.args = new Object[]{lazyTypeSpec};
            return getStatement;
        }

        if (returnTypeStr.equals("java.lang.Class")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            return getResultStatementContext(returnTypeStr, statementPrefix, "getEntityClass()");
        }

        return getResultStatementContext(returnTypeMirror.toString(), statementPrefix, getInstanceStr);
    }

    private StatementContext getResultStatementContext(String elementTypeStr, String statementPrefix, String getInstanceStr) {
        ProcessingEnvironment env = processingContext.processingEnv();
        TypeElement typeElement = env.getElementUtils().getTypeElement(elementTypeStr);
        if (typeElement == null)
            throw new ProcessingException("Element \"" + elementTypeStr + "\" not suitable for meta processing.");

        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            if (typeElement.getKind() != ElementKind.INTERFACE)
                throw new IllegalStateException(elementTypeStr + " only interfaces allowed to be used as a meta factory.");
            return addFactoryImpl(typeElement, statementPrefix);
        }

        TypeElement scopeElement = MetaScopeProcessor.scopeEntities.get(elementTypeStr);
        ClassName scopeClassName = ClassName.get(scopeElement);
        StatementContext statementContext = new StatementContext();
        statementContext.scopeElement = scopeElement;
        statementContext.format = statementPrefix + "(($T)\n\tfactory.getMetaEntity($T.class)).$L";
        statementContext.args = new Object[]{
                ClassName.get(scopeClassName.packageName(), scopeClassName.simpleName() + JetaProcessor.METACODE_CLASS_POSTFIX
                        + "." + MetacodeUtils.getMetaNameOf(env.getElementUtils(), elementTypeStr, "_MetaEntity")),
                ClassName.bestGuess(elementTypeStr),
                getInstanceStr
        };

        return statementContext;
    }

    private int factoryIndex = 0;

    private StatementContext addFactoryImpl(TypeElement element, String statementPrefix) {
        String name = element.getSimpleName().toString() + "Impl" + factoryIndex++;
        TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()))
                .addField(ClassName.OBJECT, "scope", Modifier.PRIVATE, Modifier.FINAL)
                .addField(metaEntityFactoryTypeName, "factory", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ClassName.OBJECT, "scope")
                        .addParameter(metaEntityFactoryTypeName, "factory")
                        .addStatement("this.scope = scope")
                        .addStatement("this.factory = factory")
                        .build());

        StatementContext context = new StatementContext();
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

                StatementContext subContext = addReturnStatement(method.getReturnType(), params, "return ");
                if (context.scopeElement != null && !context.scopeElement.equals(subContext.scopeElement)) {
                    throw new ProcessingException("Factory '" + element.getQualifiedName().toString() +
                            "' has elements with different scopes");
                }

                context.scopeElement = subContext.scopeElement;
                methodSpec.addStatement(subContext.format, subContext.args);
                factoryBuilder.addMethod(methodSpec.build());
            }

        context.format = statementPrefix + "new $L(scope, factory)";
        context.args = new Object[]{name};
        context.factoryTypeSpec = factoryBuilder.build();
        return context;
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
