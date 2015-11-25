package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.observer.EventObserver;
import com.github.brooth.metacode.observer.Observer;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.observer.ObserverMetacode;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * 
 */
public class ObserverProcessor extends SimpleProcessor {

    public ObserverProcessor() {
        super(Observer.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        MetacodeContext context = ctx.metacodeContext;
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ObserverMetacode.class), masterClassName));
        ClassName handlerClassName = ClassName.get(ObserverHandler.class);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyObservers")
                .addModifiers(Modifier.PUBLIC)
                .returns(handlerClassName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addParameter(Object.class, "observable")
                .addParameter(Class.class, "observableClass");

        for (Element element : ctx.elements) {
            final Observer annotation = element.getAnnotation(Observer.class);
            String observableClass = MetacodeUtils.extractClassName(new Runnable() {
                @Override
                public void run() {
                    annotation.value();
                }
            });
            ClassName observableTypeName = ClassName.bestGuess(observableClass);
            ClassName metacodeTypeName = ClassName.bestGuess(MetacodeUtils.
                    getMetacodeOf(ctx.env.getElementUtils(), observableClass));

            List<? extends VariableElement> params = ((ExecutableElement) element).getParameters();
            if (params.size() != 1)
                throw new IllegalArgumentException("Observer method must have one parameter (event)");
            TypeName eventTypeName = TypeName.get(params.get(0).asType());

            String methodHashName = "get" +
                    CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, eventTypeName.toString())
                                    .replaceAll("\\.", "_")) + "Observers";

            TypeSpec eventObserverTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(EventObserver.class), eventTypeName))
                    .addMethod(MethodSpec.methodBuilder("onEvent")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(eventTypeName, "event")
                            .returns(void.class)
                            .addStatement("master.$N(event)", element.getSimpleName().toString())
                            .build())
                    .build();

            methodBuilder
                    .beginControlFlow("if ($T.class == observableClass)", observableTypeName)
                    .addStatement("$T handler = new $T()", handlerClassName, handlerClassName)
                    .addStatement("handler.add($T.class, $T.class,\n$T.$L(($T) observable).\nregister($L))",
                            observableTypeName, eventTypeName, metacodeTypeName, methodHashName,
                            observableTypeName, eventObserverTypeSpec)
                    .addStatement("return handler")
                    .endControlFlow();
        }
        methodBuilder.addStatement("throw new IllegalArgumentException(\"Not an observer of \" + observable.getClass())");
        builder.addMethod(methodBuilder.build());

        return false;
    }
}
