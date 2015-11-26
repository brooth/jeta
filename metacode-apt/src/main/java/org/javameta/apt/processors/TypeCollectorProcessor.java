/*
 * Copyright 2015 Oleg Khalidov
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
 */

package org.javameta.apt.processors;

import com.squareup.javapoet.*;
import org.javameta.apt.MetacodeUtils;
import org.javameta.apt.ProcessorContext;
import org.javameta.collector.TypeCollector;
import org.javameta.collector.TypeCollectorMetacode;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.base.Function;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class TypeCollectorProcessor extends SimpleProcessor {

    public TypeCollectorProcessor() {
        super(TypeCollector.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        final Element element = ctx.elements.iterator().next();
        builder.addSuperinterface(ClassName.get(TypeCollectorMetacode.class));

        TypeName annotationClassTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(Annotation.class));
        ParameterizedTypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
                ClassName.get(Class.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getTypeCollection")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(listTypeName)
                .addParameter(annotationClassTypeName, "annotation");

        List<String> annotationsStr = MetacodeUtils.extractClassesNames(new Runnable() {
            @Override
            public void run() {
                element.getAnnotation(TypeCollector.class).value();
            }
        });
        for (String annotationStr : annotationsStr) {
            Set<? extends Element> annotatedElements =
                    roundEnv.getElementsAnnotatedWith(env.getElementUtils().getTypeElement(annotationStr));

            annotatedElements = FluentIterable.from(annotatedElements)
                    .transform(new Function<Element, Element>() {
                        @Override
                        public Element apply(Element input) {
                            return MetacodeUtils.typeElementOf(input);
                        }
                    }).toSet();

            methodBuilder
                    .beginControlFlow("if(annotation == $L.class)", annotationStr)
                    .addStatement("$T result = new $T($L)", listTypeName, ParameterizedTypeName.get(
                            ClassName.get(ArrayList.class), ClassName.get(Class.class)), annotatedElements.size());

            for (Element annotatedElement : annotatedElements) {
                methodBuilder.addStatement("result.add($L.class)", annotatedElement.toString());
            }

            methodBuilder
                    .addStatement("return result")
                    .endControlFlow();
        }

        methodBuilder.addStatement("throw new IllegalArgumentException(getMasterClass() + \" doesn't collect types of \" + annotation)");
        builder.addMethod(methodBuilder.build());
        return false;
    }
}
