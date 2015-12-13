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

package org.javameta.apt.processors;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import org.javameta.apt.MetacodeContext;
import org.javameta.apt.MetacodeUtils;
import org.javameta.apt.ProcessorEnvironment;
import org.javameta.observer.EventObserver;
import org.javameta.pubsub.IdsFilter;
import org.javameta.pubsub.MetaFilter;
import org.javameta.pubsub.Subscribe;
import org.javameta.pubsub.SubscriberMetacode;
import org.javameta.pubsub.SubscriptionHandler;
import org.javameta.pubsub.TopicsFilter;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
