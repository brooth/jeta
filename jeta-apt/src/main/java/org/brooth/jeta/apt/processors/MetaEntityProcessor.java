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
import org.brooth.jeta.Constructor;
import org.brooth.jeta.apt.JetaProcessor;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.inject.MetaEntity;
import org.brooth.jeta.inject.MetaEntityMetacode;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaEntityProcessor extends AbstractProcessor {

    public MetaEntityProcessor() {
        super(MetaEntity.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (MetaScopeProcessor.scopeEntities == null)
            throw new IllegalStateException("MetaInjectProcessor must follow after MetaScopeProcessor");

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
        String extTypeStr = getExtClass(annotation);
        if (extTypeStr.equals(Void.class.getCanonicalName()))
            extTypeStr = null;

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

        ProcessingEnvironment env = processingContext.processingEnv();
        TypeElement scopeElement = MetaScopeProcessor.scopeEntities.get(ofTypeStr);
        ClassName entityScopeClassName = ClassName.get(scopeElement);
        ClassName implOfClassName = ClassName.get(entityScopeClassName.packageName(),
                entityScopeClassName.simpleName() + JetaProcessor.METACODE_CLASS_POSTFIX + "." +
                        MetacodeUtils.getMetaNameOf(env.getElementUtils(), ofTypeStr, "_MetaEntity"));

        TypeSpec.Builder implBuilder = TypeSpec.classBuilder("MetaEntityImpl")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
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

        MethodSpec.Builder extClassMethodBuilder = MethodSpec.methodBuilder("getMetaEntityExtClass")
                .addAnnotation(Override.class)
                .addAnnotation(Nullable.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.supertypeOf(
                                extTypeStr == null ? WildcardTypeName.OBJECT : ClassName.bestGuess(extTypeStr))));
        if (extTypeStr == null)
            extClassMethodBuilder.addStatement("return null");
        else
            extClassMethodBuilder.addStatement("return $T.class", ClassName.bestGuess(extTypeStr));

        builder.addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(MetaEntityMetacode.class), ofClassName, implOfClassName))
                .addMethod(MethodSpec.methodBuilder("getMetaEntityImpl").addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC).returns(implOfClassName)
                        .addStatement("return new MetaEntityImpl()").build())
                .addMethod(MethodSpec.methodBuilder("getMetaEntityOfClass").addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), ofClassName))
                        .addStatement("return $T.class", ofClassName).build())
                .addMethod(MethodSpec.methodBuilder("getMetaEntityPriority").addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC).returns(int.class)
                        .addStatement("return $L", annotation.priority()).build())
                .addMethod(extClassMethodBuilder.build());

        builder.addType(implBuilder.build());

        return false;
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
}
