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

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeContext;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.pubsub.Publish;
import org.brooth.jeta.pubsub.PublisherMetacode;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class PublishProcessor extends AbstractProcessor {

    public PublishProcessor() {
        super(Publish.class);
    }

    @Override
    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(PublisherMetacode.class), masterClassName));

        MethodSpec.Builder applyMethodSpecBuilder = MethodSpec.methodBuilder("applyPublisher")
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

            TypeName subscribersTypeName = TypeName.get(element.asType());
            FieldSpec subscribersFieldSpec = FieldSpec.builder(subscribersTypeName, fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .build();
            builder.addField(subscribersFieldSpec);

            String eventTypeStr = subscribersTypeName.toString();
            int i = eventTypeStr.indexOf('<');
            if (i == -1)
                throw new IllegalArgumentException("Not valid @Publish usage, define event type as generic of Subscribers");

            eventTypeStr = eventTypeStr.substring(i + 1, eventTypeStr.lastIndexOf('>'));
            String methodHashName = "get" +
                    CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, eventTypeStr)
                                    .replaceAll("\\.", "_")) + "Subscribers";

            MethodSpec getObserversMethodSpec = MethodSpec.methodBuilder(methodHashName)
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .returns(subscribersTypeName)
                    .beginControlFlow("if ($L == null)", fieldName)
                    .beginControlFlow("synchronized ($L)", monitorFiledName)
                    .beginControlFlow("if ($L == null)", fieldName)
                    .addStatement("$L = new $T()", fieldName, subscribersTypeName)
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("return $L", fieldName)
                    .build();
            builder.addMethod(getObserversMethodSpec);

            applyMethodSpecBuilder.addStatement("master.$L = $L()", fieldName, methodHashName);
        }
        builder.addMethod(applyMethodSpecBuilder.build());

        return false;
    }
}
