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

import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeContext;
import org.brooth.jeta.apt.ProcessorEnvironment;
import org.brooth.jeta.util.Multiton;
import org.brooth.jeta.util.MultitonMetacode;

import java.util.HashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MultitonProcessor extends AbstractProcessor {

    public MultitonProcessor() {
        super(Multiton.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.get(context.masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(MultitonMetacode.class), masterClassName));
        TypeName mapTypeName = ParameterizedTypeName.get(
                ClassName.get(HashMap.class), TypeName.OBJECT, masterClassName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getMultiton")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(masterClassName)
                .addParameter(TypeName.OBJECT, "key");

        Element element = env.elements().iterator().next();

        builder.addField(FieldSpec.builder(Object.class, "MULTITON_MONITOR")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new Object()")
                .build());

        builder.addField(FieldSpec.builder(mapTypeName, "multiton")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", mapTypeName)
                .build());

        String initStr = element.getAnnotation(Multiton.class).staticConstructor();
        if (initStr.isEmpty())
            initStr = "new $T(key)";
        else
            initStr = "$T." + initStr + "(key)";

        methodBuilder
            .addStatement("$T result = multiton.get(key)", masterClassName)
            .beginControlFlow("if(result == null)")
            .beginControlFlow("synchronized (MULTITON_MONITOR)")
            .beginControlFlow("if(!multiton.containsKey(key))")
            .addStatement("result = " + initStr,  masterClassName)
            .addStatement("multiton.put(key, result)")
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .addStatement("return result");

        builder.addMethod(methodBuilder.build());
        return false;
    }
}
