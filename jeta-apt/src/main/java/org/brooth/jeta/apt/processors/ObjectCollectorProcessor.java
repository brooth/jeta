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
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessorEnvironment;
import org.brooth.jeta.collector.ObjectCollector;
import org.brooth.jeta.collector.ObjectCollectorMetacode;
import org.brooth.jeta.util.Provider;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ObjectCollectorProcessor extends AbstractProcessor {

    public ObjectCollectorProcessor() {
        super(ObjectCollector.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        final Element element = env.elements().iterator().next();
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
                    env.roundEnv().getElementsAnnotatedWith(env.processingEnv().getElementUtils().getTypeElement(annotationStr));

            annotatedElements = FluentIterable
                    .from(annotatedElements)
                    .filter(new Predicate<Element>() {
                        @Override
                        public boolean apply(Element input) {
                            return input.getKind() == ElementKind.CLASS && !input.getModifiers().contains(Modifier.ABSTRACT);
                        }
                    })
                    .transform(new Function<Element, Element>() {
                        @Override
                        public Element apply(Element input) {
                            return MetacodeUtils.typeElementOf(input);
                        }
                    }).toSet();

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

        methodBuilder.addStatement("return null");
        builder.addMethod(methodBuilder.build());
        return false;
    }

    @Override
    public boolean isUpToDate(ProcessorEnvironment env, File masterSourceJavaFile, long prevGenLastModified) {
        return false;
    }
}
