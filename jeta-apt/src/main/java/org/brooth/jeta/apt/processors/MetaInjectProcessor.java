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
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
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
        Multimap<String, StatementSpec> statements = ArrayListMultimap.create();
        for (TypeElement scopeElement : moduleScopes) {
            for (Element element : context.elements()) {
                if (element.getKind() == ElementKind.METHOD) {
                    String methodStatement = null;
                    if (staticMeta) {
                        if (element.getModifiers().contains(Modifier.STATIC))
                            methodStatement = String.format("%1$s.%2$s",
                                    masterElement.getQualifiedName().toString(), element.getSimpleName().toString());
                    } else {
                        if (!element.getModifiers().contains(Modifier.STATIC))
                            methodStatement = String.format("master.%1$s", element.getSimpleName().toString());
                    }
                    if (methodStatement == null)
                        continue;

                    ExecutableElement methodElement = (ExecutableElement) element;
                    List<? extends VariableElement> paramElements = methodElement.getParameters();
                    Multimap<String, StatementSpec> varStatements = HashMultimap.create();
                    for (VariableElement paramElement : paramElements) {
                        StatementSpec statement = getResultStatement(scopeElement, paramElement.asType(), "", "getInstance()");
                        if (statement != null) {
                            statement.element = paramElement;
                            varStatements.put(statement.providerScopeStr, statement);
                        }
                    }

                    if (!varStatements.isEmpty()) {
                        for (String varScope : varStatements.keySet()) {
                            Collection<StatementSpec> scopeVarStatements = varStatements.get(varScope);
                            List<String> paramFormats = new ArrayList<>(paramElements.size());
                            List<Object> paramArgs = new ArrayList<>(paramElements.size());
                            for (final VariableElement paramElement : paramElements) {
                                StatementSpec varStatement = Iterables.find(scopeVarStatements, new Predicate<StatementSpec>() {
                                    @Override
                                    public boolean apply(StatementSpec input) {
                                        return input.element == paramElement;
                                    }
                                }, null);

                                if (varStatement != null) {
                                    paramFormats.add(varStatement.format);
                                    paramArgs.addAll(Arrays.asList(varStatement.args));

                                } else {
                                    paramFormats.add("$L");
                                    paramArgs.add("null");
                                }
                            }

                            StatementSpec methodStatementSpec = new StatementSpec(varScope,
                                    (methodStatement + '(' + Joiner.on(", ").join(paramFormats) + ')'),
                                    paramArgs.toArray(new Object[paramArgs.size()]));

                            if (!statements.containsEntry(varScope, methodStatementSpec)) {
                                statements.put(varScope, methodStatementSpec);
                                unhandledElements.remove(element);
                            }
                        }
                    }

                } else if (element.getKind() == ElementKind.FIELD) {
                    String fieldStatement = null;

                    if (staticMeta) {
                        if (element.getModifiers().contains(Modifier.STATIC))
                            fieldStatement = String.format("%1$s.%2$s =\n",
                                    masterElement.getQualifiedName().toString(), element.getSimpleName().toString());
                    } else {
                        if (!element.getModifiers().contains(Modifier.STATIC))
                            fieldStatement = String.format("master.%1$s = ", element.getSimpleName().toString());
                    }
                    if (fieldStatement == null)
                        continue;

                    StatementSpec statement = getResultStatement(scopeElement, element.asType(), fieldStatement, "getInstance()");
                    if (statement != null) {
                        statement.element = element;
                        if (!statements.containsEntry(statement.providerScopeStr, statement)) {
                            statements.put(statement.providerScopeStr, statement);
                            unhandledElements.remove(element);
                        }
                    }

                } else {
                    throw new ProcessingException("Unhandled injection element type " + element.getKind());
                }
            }
        }

        if (!statements.isEmpty()) {
            List<String> scopes = new ArrayList<>(statements.keySet());
            Collections.sort(scopes, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return isAssignableScope(o1, o2) ? 1 : -1;
                }
            });

            for (String scopeElement : scopes) {
                ClassName scopeClassName = ClassName.bestGuess(scopeElement);
                ClassName scopeMetacodeClassName = ClassName.get(scopeClassName.packageName(),
                        MetacodeUtils.toSimpleMetacodeName(scopeClassName.toString()), "MetaScopeImpl");
                methodBuilder
                        .beginControlFlow("if(scope.isAssignable($T.class))", scopeClassName)
                        .addStatement("final $T s = ($T) scope", scopeMetacodeClassName, scopeMetacodeClassName);

                for (StatementSpec statement : statements.get(scopeElement))
                    methodBuilder.addStatement(statement.format, statement.args);

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
            return new StatementSpec(scopeStr, statementPrefix + "$L", providerTypeSpec);
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
            return new StatementSpec(scopeStr, statementPrefix + "$L", lazyTypeSpec);
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

    private StatementSpec getFactoryStatement(TypeElement scopeElement, TypeElement element, String statementPrefix) {
        TypeSpec.Builder factoryBuilder = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()));

        String scope = null;
        List<SubStatementContext> subStatements = new ArrayList<>();
        for (Element subElement : element.getEnclosedElements())
            if (subElement.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) subElement;
                String methodName = method.getSimpleName().toString();

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
                if (subStatement == null) {
                    subStatements.add(new SubStatementContext(new StatementSpec(null, "return null"),
                            methodName, params, methodReturnType));
                    continue;
                }

                if (scope == null)
                    scope = subStatement.providerScopeStr;
                else if (!isAssignableScope(scope, subStatement.providerScopeStr))
                    scope = subStatement.providerScopeStr;

                subStatements.add(new SubStatementContext(subStatement, methodName, params, methodReturnType));
            }

        if (scope == null)
            return null;

        for (SubStatementContext subStatement : subStatements) {
            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(subStatement.methodName)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(subStatement.returnType));

            for (String paramName : subStatement.params.keySet()) {
                methodSpec.addParameter(TypeName.get(subStatement.params.get(paramName)), paramName, Modifier.FINAL);
            }

            methodSpec.addStatement(subStatement.spec.format, subStatement.spec.args);
            factoryBuilder.addMethod(methodSpec.build());
        }

        return new StatementSpec(scope, statementPrefix + "$L", factoryBuilder.build());
    }

    private static class SubStatementContext {
        private StatementSpec spec;
        private String methodName;
        private Map<String, TypeMirror> params;
        private TypeMirror returnType;

        public SubStatementContext(StatementSpec spec, String methodName, Map<String, TypeMirror> params, TypeMirror returnType) {
            this.spec = spec;
            this.methodName = methodName;
            this.params = params;
            this.returnType = returnType;
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
        Element element;

        private StatementSpec(String providerScopeStr, String format, Object... args) {
            this.providerScopeStr = providerScopeStr;
            this.format = format;
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            StatementSpec that = (StatementSpec) o;
            if (com.google.common.base.Objects.equal(providerScopeStr, that.providerScopeStr) &&
                    Objects.equal(format, that.format) &&
                    Objects.equal(element, that.element) &&
                    args != null && that.args != null &&
                    args.length == that.args.length) {
                for (int i = 0; i < args.length; i++) {
                    if (!args[i].toString().equals(that.args[i].toString()))
                        return false;
                }
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(providerScopeStr, format, element);
        }
    }
}
