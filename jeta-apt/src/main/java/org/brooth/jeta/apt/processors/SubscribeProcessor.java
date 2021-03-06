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

import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.eventbus.*;
import org.brooth.jeta.observer.EventObserver;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SubscribeProcessor extends AbstractProcessor {

    public SubscribeProcessor() {
        super(Subscribe.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(SubscriberMetacode.class), masterClassName));
        ClassName handlerClassName = ClassName.get(SubscriptionHandler.class);
        ClassName busClassName = ClassName.get(EventBus.class);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applySubscribers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(handlerClassName)
                .addParameter(busClassName, "bus")
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addStatement("$T handler = new $T()", handlerClassName, handlerClassName);

        Elements elementUtils = processingContext.processingEnv().getElementUtils();

        for (Element element : context.elements()) {
            final Subscribe annotation = element.getAnnotation(Subscribe.class);
            List<? extends VariableElement> params = ((ExecutableElement) element).getParameters();
            if (params.size() != 1)
                throw new IllegalArgumentException("Subscriber method must have one parameter (event)");
            TypeName eventTypeName = TypeName.get(params.get(0).asType());
            if (eventTypeName instanceof ParameterizedTypeName)
                eventTypeName = ((ParameterizedTypeName) eventTypeName).rawType;

            MethodSpec.Builder onEventMethodBuilder = MethodSpec.methodBuilder("onEvent")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(eventTypeName, "event")
                    .returns(void.class);

            // IdsFilter
            if (annotation.id().length > 0) {
                String[] ids = new String[annotation.id().length];
                for (int i = 0; i < ids.length; i++)
                    ids[i] = String.valueOf(annotation.id()[i]);

                onEventMethodBuilder
                        .beginControlFlow("if(!(new $T($L).accepts(null, null, event)))",
                                TypeName.get(IdsFilter.class), Joiner.on(", ").join(ids))
                        .addStatement("return")
                        .endControlFlow();
            }
            // TopicsFilter
            if (annotation.topic().length > 0) {
                onEventMethodBuilder
                        .beginControlFlow("if(!(new $T($L).accepts(null, null, event)))",
                                TypeName.get(TopicsFilter.class), '"' + Joiner.on("\", \"").join(annotation.topic()) + '"')
                        .addStatement("return")
                        .endControlFlow();
            }

            // Filters
            String onEventMethodNameStr = element.getSimpleName().toString();
            List<?> filterList = (List<?>) MetacodeUtils.getAnnotationValue(element, annotationElement, "filters");
            if (filterList != null) {
                for (Object filterStr : filterList) {
                    String filter = filterStr.toString().replace(".class", "");
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
            }

            MethodSpec onEventMethodSpec = onEventMethodBuilder
                    .addStatement("master.$N(event)", onEventMethodNameStr)
                    .build();

            TypeSpec eventObserverTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(EventObserver.class), eventTypeName))
                    .addMethod(onEventMethodSpec)
                    .build();

            methodBuilder.addStatement("handler.add($T.class,\nbus.register($T.class, $L, $L))",
                    eventTypeName, eventTypeName, eventObserverTypeSpec, annotation.priority());
        }

        methodBuilder.addStatement("return handler");
        builder.addMethod(methodBuilder.build());

        return false;
    }
}
