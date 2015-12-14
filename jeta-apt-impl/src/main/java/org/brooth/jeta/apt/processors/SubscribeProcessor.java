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
import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeContext;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessorEnvironment;
import org.brooth.jeta.observer.EventObserver;
import org.brooth.jeta.pubsub.*;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SubscribeProcessor extends SimpleProcessor {

    public SubscribeProcessor() {
        super(Subscribe.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.get(context.masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(SubscriberMetacode.class), masterClassName));
        ClassName handlerClassName = ClassName.get(SubscriptionHandler.class);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applySubscribers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(handlerClassName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addStatement("$T handler = new $T()", handlerClassName, handlerClassName);

        Elements elementUtils = env.processingEnv().getElementUtils();

        for (Element element : env.elements()) {
            final Subscribe annotation = element.getAnnotation(Subscribe.class);
            String publisherClass = MetacodeUtils.extractClassName(new Runnable() {
                @Override
                public void run() {
                    annotation.value();
                }
            });
            ClassName observableTypeName = ClassName.bestGuess(publisherClass);
            ClassName metacodeTypeName = ClassName.bestGuess(MetacodeUtils.
                    getMetacodeOf(env.processingEnv().getElementUtils(), publisherClass));

            List<? extends VariableElement> params = ((ExecutableElement) element).getParameters();
            if (params.size() != 1)
                throw new IllegalArgumentException("Subscriber method must have one parameter (event)");
            TypeName eventTypeName = TypeName.get(params.get(0).asType());


            String methodHashName = "get" +
                    CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, eventTypeName.toString())
                                    .replaceAll("\\.", "_")) + "Subscribers";

            MethodSpec.Builder onEventMethodBuilder = MethodSpec.methodBuilder("onEvent")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(eventTypeName, "event")
                    .returns(void.class);

            // IdsFilter
            if (annotation.ids().length > 0) {
                String[] ids = new String[annotation.ids().length];
                for (int i = 0; i < ids.length; i++)
                    ids[i] = String.valueOf(annotation.ids()[i]);

                onEventMethodBuilder
                        .beginControlFlow("if(!(new $T($L).accepts(null, null, event)))",
                                TypeName.get(IdsFilter.class), Joiner.on(", ").join(ids))
                        .addStatement("return")
                        .endControlFlow();
            }
            // TopicsFilter
            if (annotation.topics().length > 0) {
                onEventMethodBuilder
                        .beginControlFlow("if(!(new $T($L).accepts(null, null, event)))",
                                TypeName.get(TopicsFilter.class), '"' + Joiner.on("\", \"").join(annotation.topics()) + '"')
                        .addStatement("return")
                        .endControlFlow();
            }

            // Filters
            String onEventMethodNameStr = element.getSimpleName().toString();
            List<String> filters = MetacodeUtils.extractClassesNames(new Runnable() {
                @Override
                public void run() {
                    annotation.filters();
                }
            });
            for (String filter : filters) {
                TypeElement filterTypeElement = elementUtils.getTypeElement(filter);
                if (filterTypeElement.getKind() == ElementKind.CLASS) {
                    onEventMethodBuilder
                            .beginControlFlow("if(!(new $T().accepts(master, \"$L\", event)))",
                                    ClassName.bestGuess(filter), onEventMethodNameStr)
                            .addStatement("return")
                            .endControlFlow();

                } else {
                    MetaFilter metaFilter = filterTypeElement.getAnnotation(MetaFilter.class);
                    if (metaFilter == null)
                        throw new IllegalArgumentException("Not valid Filter usage. '" + filter
                                + "' must be implementation of Filter"
                                + " or interface annotated with MetaFilter");

                    String expression = metaFilter.emitExpression()
                            .replaceAll("\\$m", "master")
                            .replaceAll("\\$e", "event");

                    onEventMethodBuilder
                            .beginControlFlow("if(!($L))", expression)
                            .addStatement("return")
                            .endControlFlow();
                }
            }

            MethodSpec onEventMethodSpec = onEventMethodBuilder
                    .addStatement("master.$N(event)", onEventMethodNameStr)
                    .build();

            TypeSpec eventObserverTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(EventObserver.class), eventTypeName))
                    .addMethod(onEventMethodSpec)
                    .build();

            methodBuilder.addStatement("handler.add($T.class, $T.class,\n$T.$L().\nregister($L, $L))",
                            observableTypeName, eventTypeName, metacodeTypeName, methodHashName, 
                            eventObserverTypeSpec, annotation.priority());
        }

        methodBuilder.addStatement("return handler");
        builder.addMethod(methodBuilder.build());

        return false;
    }
}
