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
import org.brooth.jeta.apt.ProcessorEnvironment;
import org.brooth.jeta.util.Implementation;
import org.brooth.jeta.util.ImplementationMetacode;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.File;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ImplementationProcessor extends AbstractProcessor {

    public ImplementationProcessor() {
        super(Implementation.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        Element element = env.elements().iterator().next();
        final Implementation annotation = element.getAnnotation(Implementation.class);
        String implOfClassStr = MetacodeUtils.extractClassName(new Runnable() {
            @Override
            public void run() {
                annotation.value();
            }
        });
        ClassName implOfClassName = ClassName.bestGuess(implOfClassStr);

        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ImplementationMetacode.class), implOfClassName));

        String initStr = annotation.staticConstructor();
        if (initStr.isEmpty())
            initStr = "new $T()";
        else
            initStr = "$T." + initStr + "()";

        builder.addMethod(MethodSpec.methodBuilder("getImplementation")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(implOfClassName)
                .addStatement("return " + initStr, ClassName.get(env.metacodeContext().masterElement()))
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
                .addStatement("return $L", annotation.priority())
                .build());

        return false;
    }

    @Override
    public boolean isUpToDate(ProcessorEnvironment env, File masterSourceJavaFile, long prevGenLastModified) {
        return false;
    }
}
