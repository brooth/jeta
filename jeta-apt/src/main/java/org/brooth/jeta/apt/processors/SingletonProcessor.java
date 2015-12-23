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

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import org.brooth.jeta.apt.MetacodeContext;
import org.brooth.jeta.util.Singleton;
import org.brooth.jeta.util.SingletonMetacode;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonProcessor extends AbstractProcessor {

    public SingletonProcessor() {
        super(Singleton.class);
    }

    @Override
    public boolean process(TypeSpec.Builder builder, RoundEnvironment roundEnv, int round) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.get(context.masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(SingletonMetacode.class), masterClassName));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getSingleton")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(masterClassName);

        Element element = env.elements().iterator().next();

        builder.addField(FieldSpec.builder(Object.class, "SINGLETON_MONITOR")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new Object()")
                .build());

        builder.addField(FieldSpec.builder(masterClassName, "singleton")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("null")
                .build());

        String initStr = element.getAnnotation(Singleton.class).staticConstructor();
        if (initStr.isEmpty())
            initStr = "new $T()";
        else
            initStr = "$T." + initStr + "()";

        methodBuilder
            .beginControlFlow("if(singleton == null)")
            .beginControlFlow("synchronized (SINGLETON_MONITOR)")
            .beginControlFlow("if(singleton == null)")
            .addStatement("singleton = " + initStr, masterClassName)
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .addStatement("return singleton");

        builder.addMethod(methodBuilder.build());
        return false;
    }
}
