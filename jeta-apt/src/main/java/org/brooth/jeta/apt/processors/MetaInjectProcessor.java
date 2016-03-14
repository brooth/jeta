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

    private Multimap<String, TypeElement> scopeEntities;

    private static class StatementContext {
        TypeSpec factoryTypeSpec;
        Collection<TypeElement> scopes;
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
        if (context.round() == 1) {
            if (scopes == null) {
                scopes = context.roundEnv().getElementsAnnotatedWith(Scope.class);
                if (scopes.isEmpty())
                    throw new ProcessingException("No meta scope found. You need to define one and pot @Scope annotation on it.");
            }
            return true;
        }

        if (scopeEntities == null) {
            collectModuleScopes();
        }

        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(InjectMetacode.class), masterClassName));

        for (Boolean staticMeta = true; staticMeta != null; staticMeta = staticMeta ? false : null) {
            Multimap<TypeElement, StatementContext> statementContexts = HashMultimap.create();

            MethodSpec.Builder methodBuilder;
            if (staticMeta) {
                methodBuilder = MethodSpec.methodBuilder("applyStaticMeta")
                        .addParameter(metaScopeTypeName, "scope", Modifier.FINAL);

            } else {
                methodBuilder = MethodSpec.methodBuilder("applyMeta")
                        .addParameter(metaScopeTypeName, "scope", Modifier.FINAL)
                        .addParameter(masterClassName, "master", Modifier.FINAL);
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

                for (TypeElement scopeElement : statementContext.scopes) {
                    if (statementContexts.containsKey(scopeElement))
                        statementContexts.get(scopeElement).add(statementContext);
                    else
                        statementContexts.put(scopeElement, statementContext);
                }
            }

            for (TypeElement scopeElement : statementContexts.keySet()) {
                ClassName scopeMetacodeClassName = ClassName.get(processingContext.processingEnv().getElementUtils()
                                .getPackageOf(scopeElement).getQualifiedName().toString(),
                        scopeElement.getSimpleName().toString());
                ClassName metaScopeClassName = ClassName.get(scopeMetacodeClassName.packageName(),
                        scopeElement.getSimpleName().toString() + "_Metacode.MetaScope");

                methodBuilder
                        .beginControlFlow("if(scope.isAssignable($T.class))", scopeMetacodeClassName)
                        .addStatement("final $T s = ($T) scope", metaScopeClassName, metaScopeClassName);

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

    private void collectModuleScopes() {
        scopeEntities = HashMultimap.create();
        for (final Element module : scopes)
            collectModuleScopes((TypeElement) module);
    }

    private void collectModuleScopes(TypeElement scopeElement) {
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
        for (String scopeEntity : scopeElements)
            scopeEntities.put(scopeEntity, scopeElement);
    }

    private StatementContext addReturnStatement(TypeMirror returnTypeMirror, Map<String, TypeMirror> params, String statementPrefix) {
        ProcessingEnvironment env = processingContext.processingEnv();
        String returnTypeStr = env.getTypeUtils().erasure(returnTypeMirror).toString();
        Collection<String> paramValues = new ArrayList<String>(params.size() + 1);
        paramValues.add("s.getScope()");
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

        StatementContext statementContext = new StatementContext();
        statementContext.format = statementPrefix + "s.$L_MetaEntity().$L";
        statementContext.args = new Object[]{elementTypeStr.replaceAll("\\.", "_"), getInstanceStr};
        statementContext.scopes = scopeEntities.get(elementTypeStr);
        if (statementContext.scopes == null)
            throw new ProcessingException("Undefined scope for '" + elementTypeStr + "' element.");

        return statementContext;
    }

    private int factoryIndex = 0;

    private StatementContext addFactoryImpl(TypeElement element, String statementPrefix) {
        String name = element.getSimpleName().toString() + "Impl" + factoryIndex++;
        TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()));

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
                if (subContext.scopes.size() > 1)
                    throw new ProcessingException("Factory '" + element.getSimpleName().toString() + "' has element " +
                            subElement.getSimpleName().toString() + " available from multiple scopes");
                if (context.scopes != null && !context.scopes.equals(subContext.scopes)) {
                    // todo: allow if scope.ext. add the test for
                    throw new ProcessingException("Factory '" + element.getQualifiedName().toString() +
                            "' has elements from different scopes");
                }

                context.scopes = subContext.scopes;
                methodSpec.addStatement(subContext.format, subContext.args);
                factoryBuilder.addMethod(methodSpec.build());
            }

        TypeElement scopeElement = context.scopes.iterator().next();
        ClassName metaScopeClassName = ClassName.get(processingContext.processingEnv().getElementUtils()
                        .getPackageOf(scopeElement).getQualifiedName().toString(),
                scopeElement.getSimpleName().toString() + "_Metacode.MetaScope");

        factoryBuilder.addField(metaScopeClassName, "s", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(metaScopeClassName, "s")
                        .addStatement("this.s = s")
                        .build());

        context.format = statementPrefix + "new $L(s)";
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
