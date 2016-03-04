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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.meta.IMetaEntity;
import org.brooth.jeta.meta.MetaEntity;
import org.brooth.jeta.meta.MetaEntityMetacode;
import org.brooth.jeta.Constructor;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaEntityProcessor extends AbstractProcessor {

    public MetaEntityProcessor() {
        super(MetaEntity.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        TypeElement element = (TypeElement) context.elements().iterator().next();
        ClassName elementClassName = ClassName.get(element);
        MetaEntity annotation = element.getAnnotation(MetaEntity.class);
        String masterTypeStr = context.metacodeContext().masterElement().toString();

        String ofTypeStr = getOfClass(annotation);
        if (ofTypeStr.equals(Void.class.getCanonicalName()))
            ofTypeStr = masterTypeStr;
        String extTypeStr = getExtClass(annotation);
        if (extTypeStr.equals(Void.class.getCanonicalName()))
            extTypeStr = null;

        ClassName ofClassName = ClassName.bestGuess(ofTypeStr);
        String elementTypeStr = element.getQualifiedName().toString();

        boolean ofTypeEqElementType = masterTypeStr.equals(ofTypeStr);
        boolean isProvider = masterTypeStr.equals(elementTypeStr);

        List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
        for (Element subElement : ((TypeElement) element).getEnclosedElements()) {
            boolean validInitConstructor = subElement.getSimpleName().contentEquals("<init>") && isProvider
                    && ofTypeEqElementType && !subElement.getModifiers().contains(Modifier.PRIVATE)
                    // enums may have constructors
                    && element.getKind() == ElementKind.CLASS;

            if (validInitConstructor || subElement.getAnnotation(Constructor.class) != null)
                constructors.add((ExecutableElement) subElement);
        }

        // emit MetaEntity interface
        if (ofTypeEqElementType && !annotation.minor()) {
            ClassName superClassName = extTypeStr == null ? ClassName.get(IMetaEntity.class)
                    : ClassName.bestGuess(MetacodeUtils.getMetacodeOf(processingContext.processingEnv().getElementUtils(), extTypeStr)
                            + ".MetaEntity");

            TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder("MetaEntity")
                    .addJavadoc("emitted by @see " + elementTypeStr + '\n').addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(superClassName).addMethod(
                            MethodSpec.methodBuilder("getEntityClass")
                                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(ParameterizedTypeName
                                            .get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ofClassName)))
                                    .build());

            for (ExecutableElement constructor : constructors) {
                Iterable<ParameterSpec> params = Iterables.transform(constructor.getParameters(),
                        new Function<VariableElement, ParameterSpec>() {
                            public ParameterSpec apply(VariableElement input) {
                                return ParameterSpec
                                        .builder(TypeName.get(input.asType()), input.getSimpleName().toString())
                                        .build();
                            }
                        });

                interfaceBuilder.addMethod(
                        MethodSpec.methodBuilder("getInstance").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(ofClassName).addParameters(params).build());
            }

            builder.addType(interfaceBuilder.build());
        }

        // emit MetaEntityImpl (for autocode, host annotated @MetaEntity with
        // and not an interface/scheme)
        if (isProvider && element.getKind() != ElementKind.INTERFACE) {
            ClassName implOfClassName = ClassName
                    .bestGuess(MetacodeUtils.getMetacodeOf(processingContext.processingEnv().getElementUtils(),
                            ofTypeEqElementType ? masterTypeStr : ofTypeStr) + ".MetaEntity");

            TypeSpec.Builder implBuilder = TypeSpec.classBuilder("MetaEntityImpl")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL).addSuperinterface(implOfClassName).addMethod(
                            MethodSpec.methodBuilder("getEntityClass").addAnnotation(Override.class)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addStatement("return $T.class", ofClassName).returns(ParameterizedTypeName
                                            .get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ofClassName)))
                            .build());

            for (ExecutableElement constructor : constructors) {
                List<ParameterSpec> params2 = new ArrayList<ParameterSpec>(constructor.getParameters().size());

                String[] params = new String[constructor.getParameters().size() * 2];
                String[] paramValues = new String[params.length / 2];
                int pi = 0;
                for (VariableElement param : constructor.getParameters()) {
                    params2.add(ParameterSpec.builder(TypeName.get(param.asType()), param.getSimpleName().toString())
                            .build());

                    int vi = pi / 2;
                    params[pi++] = param.asType().toString();
                    paramValues[vi] = param.getSimpleName().toString();
                    params[pi++] = paramValues[vi];
                }

                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getInstance").addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC).returns(ofClassName).addParameters(params2);

                String paramNames = Joiner.on(", ").join(paramValues);
                if (constructor.getSimpleName().contentEquals("<init>")) {
                    methodBuilder.addStatement("return new $T($L)", ofClassName, paramNames);

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
        }
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

    @Override
    public Set<TypeElement> applicableMastersOfElement(Element element) {
        Set<TypeElement> masters = super.applicableMastersOfElement(element);
        MetaEntity annotation = element.getAnnotation(MetaEntity.class);
        String ofClass = getOfClass(annotation);

        if (!annotation.minor() && !ofClass.equals(Void.class.getCanonicalName())) {
            TypeElement of = processingContext.processingEnv().getElementUtils().getTypeElement(ofClass);
            TypeElement master = masters.iterator().next();
            if (!master.equals(of))
                masters = Sets.newHashSet(master, of);
        }

        return masters;
    }
}
