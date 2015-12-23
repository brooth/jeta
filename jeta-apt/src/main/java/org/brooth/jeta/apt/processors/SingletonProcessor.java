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
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.util.Singleton;
import org.brooth.jeta.util.SingletonMetacode;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonProcessor extends AbstractProcessor {

    public SingletonProcessor() {
        super(Singleton.class);
    }

    @Override
    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(SingletonMetacode.class), masterClassName));

        Element element = context.elements().iterator().next();

        String initStr = element.getAnnotation(Singleton.class).staticConstructor();
        if (initStr.isEmpty())
            initStr = "new $T()";
        else
            initStr = "$T." + initStr + "()";

        builder.addType(TypeSpec.classBuilder("SingletonHolder")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addField(FieldSpec.builder(masterClassName, "INSTANCE",
                        Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                        .initializer(initStr, masterClassName)
                        .build())
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getSingleton")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(masterClassName)
                .addStatement("return SingletonHolder.INSTANCE")
                .build());

        return false;
    }
}
