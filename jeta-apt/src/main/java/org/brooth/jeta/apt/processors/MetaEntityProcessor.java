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
import org.brooth.jeta.Provider;
import org.brooth.jeta.apt.*;
import org.brooth.jeta.inject.MetaEntity;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaEntityProcessor extends AbstractProcessor {

    @Nullable
    private String defaultScopeStr;

    public MetaEntityProcessor() {
        super(MetaEntity.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        defaultScopeStr = processingContext.processingProperties().getProperty("meta.scope.default", null);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (context.round() == 1)
            return true;

        TypeElement element = (TypeElement) context.elements().iterator().next();

        // is a scheme?
        if (element.getKind() != ElementKind.CLASS)
            return false;

        ClassName elementClassName = ClassName.get(element);
        final MetaEntity annotation = element.getAnnotation(MetaEntity.class);
        String masterTypeStr = context.metacodeContext().masterElement().toString();

        String ofTypeStr = getOfClass(annotation);
        if (ofTypeStr.equals(Void.class.getCanonicalName()))
            ofTypeStr = masterTypeStr;

        ClassName ofClassName = ClassName.bestGuess(ofTypeStr);
        boolean isSelfProvider = masterTypeStr.equals(ofTypeStr);

        List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
        for (Element subElement : ((TypeElement) element).getEnclosedElements()) {
            boolean validInitConstructor = element.getKind() == ElementKind.CLASS
                    && !subElement.getModifiers().contains(Modifier.PRIVATE)
                    && ((isSelfProvider && subElement.getSimpleName().contentEquals("<init>")) ||
                    subElement.getAnnotation(Constructor.class) != null);

            if (validInitConstructor)
                constructors.add((ExecutableElement) subElement);
        }

        ClassName entityScopeClassName;
        String scopeClassStr = getScopeClass(annotation);
        if (!scopeClassStr.equals(Void.class.getCanonicalName())) {
            entityScopeClassName = ClassName.bestGuess(scopeClassStr);
        } else {
            if (defaultScopeStr == null)
                throw new ProcessingException("Undefined meta entity scope");
            entityScopeClassName = ClassName.bestGuess(defaultScopeStr);
        }

        ClassName implOfClassName = ClassName.get(entityScopeClassName.packageName(),
                entityScopeClassName.simpleName() + JetaProcessor.METACODE_CLASS_POSTFIX + "." +
                        ofTypeStr.replaceAll("\\.", "_") + "_MetaEntity");

        ParameterizedTypeName providerTypeName = ParameterizedTypeName.get(ClassName.get(Provider.class), implOfClassName);

        /* todo: singleton */
        builder.addType(TypeSpec.classBuilder("MetaEntityProvider")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(providerTypeName)
                .addMethod(
                        MethodSpec.methodBuilder("get")
                                .returns(implOfClassName)
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addStatement("return new MetaEntityImpl()")
                                .build())
                .build());

        TypeSpec.Builder implBuilder = TypeSpec.classBuilder("MetaEntityImpl")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(implOfClassName)
                .addMethod(MethodSpec.methodBuilder("getEntityClass")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return $T.class", ofClassName).returns(ParameterizedTypeName
                                .get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ofClassName)))
                        .build());

        for (ExecutableElement constructor : constructors) {
            List<ParameterSpec> params = new ArrayList<ParameterSpec>(constructor.getParameters().size());
            List<String> paramValues = new ArrayList<String>(params.size());
            params.add(ParameterSpec.builder(ClassName.OBJECT, "__scope__").build());

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

            String paramNames = Joiner.on(", ").join(paramValues);
            if (constructor.getSimpleName().contentEquals("<init>")) {
                methodBuilder.addStatement("return new $T($L)", elementClassName, paramNames);

            } else {
                String initCode = constructor.getModifiers().contains(Modifier.STATIC) ? "$T"
                        : annotation.staticConstructor().isEmpty() ? ("new $T()")
                        : String.format("$T.%s()", annotation.staticConstructor());

                methodBuilder.addStatement("return " + initCode + ".$L($L)", elementClassName,
                        constructor.getSimpleName().toString(), paramNames);
            }

            implBuilder.addMethod(methodBuilder.build());
        }

        builder.addType(implBuilder.build());
        return false;
    }

    private String getScopeClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.scope();
            }
        });
    }

    private String getOfClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.of();
            }
        });
    }
}
