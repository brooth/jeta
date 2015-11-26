package org.javameta.apt;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import org.javameta.observer.EventObserver;
import org.javameta.pubsub.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

/**
 *
 */
public class SubscribeProcessor extends SimpleProcessor {

    public SubscribeProcessor() {
        super(Subscribe.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        MetacodeContext context = ctx.metacodeContext;
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(SubscriberMetacode.class), masterClassName));
        ClassName handlerClassName = ClassName.get(SubscriptionHandler.class);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applySubscribers")
                .addModifiers(Modifier.PUBLIC)
                .returns(handlerClassName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addStatement("$T handler = new $T()", handlerClassName, handlerClassName);

        Types typeUtils = env.getTypeUtils();
        Elements elementUtils = env.getElementUtils();

        for (Element element : ctx.elements) {
            final Subscribe annotation = element.getAnnotation(Subscribe.class);
            String publisherClass = MetacodeUtils.extractClassName(new Runnable() {
                @Override
                public void run() {
                    annotation.value();
                }
            });
            ClassName observableTypeName = ClassName.bestGuess(publisherClass);
            ClassName metacodeTypeName = ClassName.bestGuess(MetacodeUtils.
                    getMetacodeOf(env.getElementUtils(), publisherClass));

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

            methodBuilder.addStatement("handler.add($T.class, $T.class,\n$T.$L().\nregister($L))",
                            observableTypeName, eventTypeName, metacodeTypeName, methodHashName, eventObserverTypeSpec);
        }

        methodBuilder.addStatement("return handler");
        builder.addMethod(methodBuilder.build());

        return false;
    }
}
