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

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.observer.ObservableMetacode;
import org.brooth.jeta.observer.Subject;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ObservableProcessor extends AbstractProcessor {

    public ObservableProcessor() {
        super(Subject.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ObservableMetacode.class), masterClassName));

        MethodSpec.Builder applyMethodSpecBuilder = MethodSpec.methodBuilder("applyObservable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(masterClassName, "master");

        for (Element element : context.elements()) {
            String fieldName = element.getSimpleName().toString();

            String monitorFiledName = fieldName + "_MONITOR";
            builder.addField(FieldSpec.builder(Object.class, monitorFiledName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new Object()")
                    .build());

            TypeName observersTypeName = TypeName.get(element.asType());
            TypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                    masterClassName, observersTypeName);
            FieldSpec observersField = FieldSpec.builder(mapTypeName, fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new $T()", ParameterizedTypeName.get(ClassName.get(WeakHashMap.class),
                            masterClassName, observersTypeName))
                    .build();
            builder.addField(observersField);

            String eventTypeStr = observersTypeName.toString();
            int i = eventTypeStr.indexOf('<');
            if (i == -1)
                throw new IllegalArgumentException("Not valid @Subject usage, define event type as generic of Observers");

            eventTypeStr = eventTypeStr.substring(i + 1, eventTypeStr.lastIndexOf('>'));
            String methodHashName = "get" +
                    CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, eventTypeStr)
                                    .replaceAll("\\.", "_")) + "Observers";

            MethodSpec getObserversMethodSpec = MethodSpec.methodBuilder(methodHashName)
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .returns(observersTypeName)
                    .addParameter(masterClassName, "master")
                    .addStatement("$T result = $L.get(master)", observersTypeName, fieldName)
                    .beginControlFlow("if (result == null)")
                    .beginControlFlow("synchronized ($L)", monitorFiledName)
                    .beginControlFlow("if (!$L.containsKey(master))", fieldName)
                    .addStatement("result = new $T()", observersTypeName)
                    .addStatement("$L.put(master, result)", fieldName)
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("return result")
                    .build();
            builder.addMethod(getObserversMethodSpec);

            applyMethodSpecBuilder.addStatement("master.$L = $L(master)", fieldName, methodHashName);
        }
        builder.addMethod(applyMethodSpecBuilder.build());

        return false;
    }
}
