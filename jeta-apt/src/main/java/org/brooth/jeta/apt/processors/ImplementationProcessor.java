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

package org.brooth.jeta.apt.processors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.util.Implementation;
import org.brooth.jeta.util.ImplementationMetacode;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ImplementationProcessor extends AbstractProcessor {

    public ImplementationProcessor() {
        super(Implementation.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        Element element = context.elements().iterator().next();
        final AnnotationMirror annotation = MetacodeUtils.getAnnotation(element, annotationElement);
        Object value = MetacodeUtils.getAnnotationValue(annotation, "value");
        if (value == null)
            throw new ProcessingException("Failed to process " + element.toString() + ", check its source code for compilation errors");
        ClassName implOfClassName = ClassName.bestGuess(value.toString());

        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ImplementationMetacode.class), implOfClassName));

        String initStr = (String) MetacodeUtils.getAnnotationValue(annotation, "staticConstructor");
        if (initStr == null)
            initStr = "new $T()";
        else
            initStr = "$T." + initStr + "()";

        builder.addMethod(MethodSpec.methodBuilder("getImplementation")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(implOfClassName)
                .addStatement("return " + initStr, ClassName.get(context.metacodeContext().masterElement()))
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getImplementationOf")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(
                        ClassName.get(Class.class), implOfClassName))
                .addStatement("return $T.class", implOfClassName)
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getImplementationPriority")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addStatement("return $L", element.getAnnotation(Implementation.class).priority())
                .build());

        return false;
    }

    @Override
    public boolean ignoreUpToDate() {
        return true;
    }
}
