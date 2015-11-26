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
import org.javameta.collector.ObjectCollector;
import org.javameta.collector.ObjectCollectorMetacode;
import org.javameta.util.Provider;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ObjectCollectorProcessor extends SimpleProcessor {

    public ObjectCollectorProcessor() {
        super(ObjectCollector.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        // it's enough one element to collect the type
        final Element element = ctx.elements.iterator().next();
        builder.addSuperinterface(ClassName.get(ObjectCollectorMetacode.class));

        TypeName annotationClassTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(Annotation.class));
        TypeName providerTypeName = ParameterizedTypeName.get(ClassName.get(Provider.class),
                WildcardTypeName.subtypeOf(Object.class));
        TypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class), providerTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getObjectCollection")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(listTypeName)
                .addParameter(annotationClassTypeName, "annotation");

        List<String> annotationsStr = MetacodeUtils.extractClassesNames(new Runnable() {
            @Override
            public void run() {
                element.getAnnotation(ObjectCollector.class).value();
            }
        });
        for (String annotationStr : annotationsStr) {
            Set<? extends Element> annotatedElements =
                    roundEnv.getElementsAnnotatedWith(env.getElementUtils().getTypeElement(annotationStr));

            methodBuilder
                    .beginControlFlow("if(annotation == $L.class)", annotationStr)
                    .addStatement("$T result = new $T($L)", listTypeName, ParameterizedTypeName.get(
                            ClassName.get(ArrayList.class), providerTypeName), annotatedElements.size());

            for (Element annotatedElement : annotatedElements) {
                TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Provider.class), TypeName.OBJECT))
                        .addMethod(MethodSpec.methodBuilder("get")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(Object.class)
                                .addStatement("return new $L()", MetacodeUtils.typeElementOf(annotatedElement).toString())
                                .build())
                        .build();

                methodBuilder.addStatement("result.add($L)", providerTypeSpec);
            }

            methodBuilder
                    .addStatement("return result")
                    .endControlFlow();
        }

        methodBuilder.addStatement("throw new IllegalArgumentException(getMasterClass() + \" doesn't collect objects of \" + annotation)");
        builder.addMethod(methodBuilder.build());
        return false;
    }
}
