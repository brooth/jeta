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
import com.squareup.javapoet.*;
import org.brooth.jeta.Constructor;
import org.brooth.jeta.apt.*;
import org.brooth.jeta.inject.Producer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ProducerProcessor extends AbstractProcessor {

    @Nullable
    private String defaultScopeStr;

    public ProducerProcessor() {
        super(Producer.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        defaultScopeStr = processingContext.processingProperties().getProperty("inject.scope.default", null);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (context.round() == 1)
            return true;

        TypeElement element = (TypeElement) context.elements().iterator().next();
        ClassName elementClassName = ClassName.get(element);
        final Producer annotation = element.getAnnotation(Producer.class);
        String masterTypeStr = context.metacodeContext().masterElement().toString();

        String ofTypeStr = getOfClass(annotation);
        if (isVoid(ofTypeStr))
            ofTypeStr = masterTypeStr;

        ClassName ofClassName = ClassName.bestGuess(ofTypeStr);
        boolean isSelfProvider = masterTypeStr.equals(ofTypeStr);

        List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
        for (Element subElement : ((TypeElement) element).getEnclosedElements()) {
            boolean validInitConstructor = !subElement.getModifiers().contains(Modifier.PRIVATE)
                    && ((isSelfProvider && subElement.getSimpleName().contentEquals("<init>")) ||
                    subElement.getAnnotation(Constructor.class) != null);
            if (validInitConstructor)
                constructors.add((ExecutableElement) subElement);
        }

        ClassName entityScopeClassName;
        String scopeClassStr = getScopeClass(annotation);
        if (!isVoid(scopeClassStr)) {
            entityScopeClassName = ClassName.bestGuess(scopeClassStr);
        } else {
            if (defaultScopeStr == null)
                throw new ProcessingException("Undefined meta entity scope");
            entityScopeClassName = ClassName.bestGuess(defaultScopeStr);
        }

        // think it's a bag in java poet.
        ClassName metaScopeClassName = ClassName.get(entityScopeClassName.packageName(),
                entityScopeClassName.simpleName() + JetaProcessor.METACODE_CLASS_POSTFIX);
        ClassName implOfClassName = ClassName.get(metaScopeClassName.packageName() + '.' + metaScopeClassName.simpleName(),
                (ofTypeStr.replaceAll("\\.", "_") + "_MetaProducer"));

        TypeSpec.Builder implBuilder = TypeSpec.classBuilder("MetaProducerImpl")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(implOfClassName)
                .addField(FieldSpec.builder(entityScopeClassName, "__scope__", Modifier.PUBLIC).build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(entityScopeClassName, "__scope__")
                        .addStatement("this.__scope__ = __scope__")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getEntityClass")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return $T.class", ofClassName)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ofClassName)))
                        .build())
                .addMethod(MethodSpec.methodBuilder("isImplemented")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return " + !element.getKind().isInterface())
                        .returns(boolean.class)
                        .build());

        if (annotation.singleton())
            implBuilder.addField(ofClassName, "instance", Modifier.PRIVATE, Modifier.VOLATILE);

        for (ExecutableElement constructor : constructors) {
            List<ParameterSpec> params = new ArrayList<ParameterSpec>(constructor.getParameters().size());
            List<String> paramValues = new ArrayList<String>(params.size());
            for (VariableElement param : constructor.getParameters()) {
                TypeMirror paramType = param.asType();
                String paramName = param.getSimpleName().toString();
                if (paramName.equals("__scope__")) {
                    paramValues.add(paramName);

                } else {
                    params.add(ParameterSpec.builder(TypeName.get(paramType), paramName).build());
                    paramValues.add(paramName);
                }
            }

            MethodSpec.Builder methodBuilder = MethodSpec
                    .methodBuilder("getInstance")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ofClassName)
                    .addParameters(params);

            if (element.getKind().isInterface()) {
                methodBuilder.addStatement("return null");

            } else {
                String paramNames = Joiner.on(", ").join(paramValues);
                String assignPrefix;

                if (annotation.singleton()) {
                    methodBuilder
                            .beginControlFlow("if(instance == null)")
                            .beginControlFlow("synchronized(this)")
                            .beginControlFlow("if(instance == null)");
                    assignPrefix = "instance = ";
                } else {
                    assignPrefix = "return ";
                }

                if (constructor.getSimpleName().contentEquals("<init>")) {
                    methodBuilder.addStatement(assignPrefix + "new $T($L)", elementClassName, paramNames);

                } else {
                    String initCode = constructor.getModifiers().contains(Modifier.STATIC) ? "$T"
                            : annotation.staticConstructor().isEmpty() ? ("new $T()")
                            : String.format("$T.%s()", annotation.staticConstructor());

                    methodBuilder.addStatement(assignPrefix + initCode + ".$L($L)", elementClassName,
                            constructor.getSimpleName().toString(), paramNames);
                }

                if (annotation.singleton()) {
                    methodBuilder
                            .endControlFlow()
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return instance");
                }
            }

            implBuilder.addMethod(methodBuilder.build());
        }

        builder.addType(implBuilder.build());
        return false;
    }

    private boolean isVoid(String str) {
        return str.equals(Void.class.getCanonicalName());
    }

    @Nonnull
    private String getScopeClass(final Producer annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.scope();
            }
        });
    }

    @Nonnull
    private String getOfClass(final Producer annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.of();
            }
        });
    }
}
