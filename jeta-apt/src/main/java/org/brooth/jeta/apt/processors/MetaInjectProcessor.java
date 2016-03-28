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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.squareup.javapoet.*;
import org.brooth.jeta.Factory;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingContext;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.inject.Inject;
import org.brooth.jeta.inject.InjectMetacode;
import org.brooth.jeta.inject.MetaScope;
import org.brooth.jeta.inject.Module;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaInjectProcessor extends AbstractLookupScopeProcessor {

    private final TypeName metaScopeTypeName = ParameterizedTypeName.get(
            ClassName.get(MetaScope.class), WildcardTypeName.subtypeOf(TypeName.OBJECT));

    private TypeElement module;
    private List<TypeElement> moduleScopes;

    private String providerAlias = null;


    public MetaInjectProcessor() {
        super(Inject.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        providerAlias = processingContext.processingProperties().getProperty("inject.alias.provider", null);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (context.round() == 1) {
            if (module == null) {
                Set<? extends Element> modules = context.roundEnv().getElementsAnnotatedWith(Module.class);
                if (modules.isEmpty())
                    throw new ProcessingException("No module defined. Create one and put @Module on it.");
                module = (TypeElement) modules.iterator().next();

                List<String> scopes = MetacodeUtils.extractClassesNames(new Runnable() {
                    @Override
                    public void run() {
                        module.getAnnotation(Module.class).scopes();
                    }
                });
                moduleScopes = Lists.transform(scopes, new Function<String, TypeElement>() {
                    @Override
                    public TypeElement apply(String input) {
                        return processingContext.processingEnv().getElementUtils().getTypeElement(input);
                    }
                });
            }
            return true;
        }

        TypeElement masterElement = context.metacodeContext().masterElement();
        ClassName masterClassName = ClassName.get(masterElement);
        builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(InjectMetacode.class), masterClassName));

        ArrayList<Element> unhandledElements = new ArrayList<>(context.elements());
        buildInjectMethod(builder, context, masterElement, masterClassName, false, unhandledElements);
        buildInjectMethod(builder, context, masterElement, masterClassName, true, unhandledElements);
        if (!unhandledElements.isEmpty()) {
            List<String> elements = new ArrayList<>();
            for (Element element : unhandledElements)
                elements.add(element.getEnclosingElement().toString() + "->" + element.toString());
            throw new ProcessingException("No scope found to provide injection for {" +
                    Joiner.on(", ").join(elements) + "}");
        }

        return false;
    }

    private void buildInjectMethod(TypeSpec.Builder builder, RoundContext context, TypeElement masterElement,
                                   ClassName masterClassName, Boolean staticMeta, ArrayList<Element> unhandledElements) {
        MethodSpec.Builder methodBuilder;
        if (staticMeta) {
            methodBuilder = MethodSpec.methodBuilder("injectStatic")
                    .addParameter(metaScopeTypeName, "scope", Modifier.FINAL);

        } else {
            methodBuilder = MethodSpec.methodBuilder("inject")
                    .addParameter(metaScopeTypeName, "scope", Modifier.FINAL)
                    .addParameter(masterClassName, "master", Modifier.FINAL);
        }

        methodBuilder.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(void.class);
        Multimap<String, StatementSpec> statements = HashMultimap.create();
        for (TypeElement scopeElement : moduleScopes) {
            for (Element element : context.elements()) {
                String fieldNameStr = element.getSimpleName().toString();
                String fieldStatement = null;

                if (staticMeta) {
                    if (element.getModifiers().contains(Modifier.STATIC))
                        fieldStatement = String.format("%1$s.%2$s =\n",
                                masterElement.getQualifiedName().toString(), fieldNameStr);

                } else {
                    if (!element.getModifiers().contains(Modifier.STATIC))
                        fieldStatement = String.format("master.%1$s = ", fieldNameStr);
                }
                if (fieldStatement == null)
                    continue;

                StatementSpec statement = getResultStatement(scopeElement, element.asType(), fieldStatement, "getInstance()");
                if (statement != null) {
                    statement.element = element;
                    if (!(statements.containsKey(statement.providerScopeStr) &&
                            statements.get(statement.providerScopeStr).contains(statement)))
                        statements.put(statement.providerScopeStr, statement);

                    unhandledElements.remove(element);
                }
            }
        }

        if (!statements.isEmpty()) {
            for (String scopeElement : statements.keySet()) {
                ClassName scopeClassName = ClassName.bestGuess(scopeElement);
                ClassName scopeMetacodeClassName = ClassName.get(scopeClassName.packageName(),
                        MetacodeUtils.toSimpleMetacodeName(scopeClassName.toString()), "MetaScopeImpl");
                methodBuilder
                        .beginControlFlow("if(scope.isAssignable($T.class))", scopeClassName)
                        .addStatement("final $T s = ($T) scope", scopeMetacodeClassName, scopeMetacodeClassName);

                for (StatementSpec statement : statements.get(scopeElement)) {
                    methodBuilder.addStatement(statement.format, statement.args);
                    if (statement.factory != null)
                        builder.addType(statement.factory);
                }

                methodBuilder.endControlFlow();
            }
        }

        builder.addMethod(methodBuilder.build());
    }

    @Nullable
    private StatementSpec getResultStatement(TypeElement scopeElement, TypeMirror returnTypeMirror, String statementPrefix, String getInstanceStr) {
        ProcessingEnvironment env = processingContext.processingEnv();
        String returnTypeStr = env.getTypeUtils().erasure(returnTypeMirror).toString();

        if (returnTypeStr.equals("org.brooth.jeta.Provider") || returnTypeStr.equals(providerAlias)) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            String scopeStr = lookupEntityScope(module, scopeElement.getQualifiedName().toString(), returnTypeStr);
            if (scopeStr == null)
                return null;

            StatementSpec statement = getAssignmentStatement(scopeStr, returnTypeStr, "return ", getInstanceStr);
            TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(TypeName.get(returnTypeMirror))
                    .addMethod(MethodSpec.methodBuilder("get")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ClassName.bestGuess(returnTypeStr))
                            .addStatement(statement.format, statement.args)
                            .build())
                    .build();
            return new StatementSpec(scopeStr, statementPrefix + " $L", providerTypeSpec);
        }

        if (returnTypeStr.equals("org.brooth.jeta.Lazy")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            String scopeStr = lookupEntityScope(module, scopeElement.getQualifiedName().toString(), returnTypeStr);
            if (scopeStr == null)
                return null;

            ClassName returnClassName = ClassName.bestGuess(returnTypeStr);
            StatementSpec statement = getAssignmentStatement(scopeStr, returnTypeStr, "instance = ", getInstanceStr);
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
            return new StatementSpec(scopeStr, statementPrefix + " $L", lazyTypeSpec);
        }

        if (returnTypeStr.equals("java.lang.Class")) {
            returnTypeStr = getGenericType(returnTypeMirror.toString());
            String scopeStr = lookupEntityScope(module, scopeElement.getQualifiedName().toString(), returnTypeStr);
            if (scopeStr == null)
                return null;
            return getAssignmentStatement(scopeStr, returnTypeStr, statementPrefix, "getEntityClass()");
        }

        TypeElement typeElement = (TypeElement) env.getTypeUtils().asElement(returnTypeMirror);
        if (typeElement == null)
            throw new ProcessingException(returnTypeStr + " is not valid element for meta injection");

        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            if (typeElement.getKind() != ElementKind.INTERFACE)
                throw new IllegalStateException(returnTypeStr + " not allowed. Only interfaces can be used as a meta factory.");
            return getFactoryStatement(scopeElement, typeElement, statementPrefix);
        }
        String scopeStr = lookupEntityScope(module, scopeElement.getQualifiedName().toString(), returnTypeStr);
        if (scopeStr == null)
            return null;
        return getAssignmentStatement(scopeStr, returnTypeStr, statementPrefix, getInstanceStr);
    }

    private StatementSpec getAssignmentStatement(String scopeStr, String elementTypeStr, String statementPrefix, String getInstanceStr) {
        return new StatementSpec(scopeStr, statementPrefix + "s.$L_$L_MetaEntity().$L",
                elementTypeStr.replaceAll("\\.", "_"), ClassName.bestGuess(scopeStr).simpleName(), getInstanceStr);
    }

    private int factoryIndex = 0;

    private StatementSpec getFactoryStatement(TypeElement scopeElement, TypeElement element, String statementPrefix) {
        String name = element.getSimpleName().toString() + "Impl" + factoryIndex++;

        TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()));

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

        ClassName scopeClassName = ClassName.get(scopeElement);
        ClassName scopeMetacodeClassName = ClassName.get(scopeClassName.packageName(),
                MetacodeUtils.toSimpleMetacodeName(scopeClassName.toString()), "MetaScopeImpl");

        StatementSpec statement = new StatementSpec(scopeElement.getQualifiedName().toString(),
                statementPrefix + "new $L(s)", name);
        statement.factory = factoryBuilder
                .addField(scopeMetacodeClassName, "s", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(scopeMetacodeClassName, "s")
                        .addStatement("this.s = s")
                        .build())
                .build();
        return statement;
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
        String metaAlias = processingContext.processingProperties().getProperty("inject.alias", "");
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

    private static class StatementSpec {
        String providerScopeStr;
        String format;
        Object[] args;
        TypeSpec factory;
        Element element;

        private StatementSpec(String providerScopeStr, String format, Object... args) {
            this.providerScopeStr = providerScopeStr;
            this.format = format;
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StatementSpec that = (StatementSpec) o;

            return element != null ? element.equals(that.element) : that.element == null;
        }

        @Override
        public int hashCode() {
            return element != null ? element.hashCode() : 0;
        }
    }
}
