/*
 * Copyright 2016 Oleg Khalidov
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
import org.brooth.jeta.util.Multiton;
import org.brooth.jeta.util.MultitonMetacode;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.concurrent.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MultitonProcessor extends AbstractProcessor {

    public MultitonProcessor() {
        super(Multiton.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(MultitonMetacode.class), masterClassName));
        TypeName futureTypeName = ParameterizedTypeName.get(
                ClassName.get(Future.class), masterClassName);
        TypeName futureTaskTypeName = ParameterizedTypeName.get(
                ClassName.get(FutureTask.class), masterClassName);
        TypeName mapTypeName = ParameterizedTypeName.get(
                ClassName.get(ConcurrentHashMap.class), TypeName.OBJECT, futureTypeName);

        Element element = context.elements().iterator().next();

        builder.addField(FieldSpec.builder(mapTypeName, "multiton")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", mapTypeName)
                .build());

        String initStr = element.getAnnotation(Multiton.class).staticConstructor();
        if (initStr.isEmpty())
            initStr = "new $T(key)";
        else
            initStr = "$T." + initStr + "(key)";

        TypeSpec.Builder creatorBuilder = TypeSpec.classBuilder("MultitonCreator")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(Callable.class), masterClassName))
                .addField(TypeName.OBJECT, "key")
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(TypeName.OBJECT, "key")
                        .addStatement("this.key = key")
                        .build())
                .addMethod(MethodSpec.methodBuilder("call")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addException(TypeName.get(Exception.class))
                        .addStatement("return " + initStr, masterClassName)
                        .returns(masterClassName)
                        .build());
        builder.addType(creatorBuilder.build());

        builder.addMethod(MethodSpec.methodBuilder("getMultitonSafe")
                .addAnnotation(Override.class)
                .addException(TypeName.get(ExecutionException.class))
                .addException(TypeName.get(InterruptedException.class))
                .addModifiers(Modifier.PUBLIC)
                .returns(masterClassName)
                .addParameter(TypeName.OBJECT, "key")
                .addStatement("$T result = multiton.get(key)", futureTypeName)
                .beginControlFlow("if(result == null)")
                .addStatement("$T creator = new $T(new MultitonCreator(key))",
                        futureTaskTypeName, futureTaskTypeName)
                .addStatement("result = multiton.putIfAbsent(key, creator)")
                .beginControlFlow("if(result == null)")
                .addStatement("result = creator")
                .addStatement("creator.run()")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result.get()")
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getMultiton")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(masterClassName)
                .addParameter(TypeName.OBJECT, "key")
                .beginControlFlow("try")
                .addStatement("return getMultitonSafe(key)")
                .endControlFlow()
                .beginControlFlow("catch($T e)", TypeName.get(Exception.class))
                .addStatement("throw new $T(e)", TypeName.get(RuntimeException.class))
                .endControlFlow()
                .build());

        return false;
    }
}
