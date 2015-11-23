package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.observer.Observer;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.observer.ObserverServant;
import com.github.brooth.metacode.observer.Observers;
import com.squareup.javapoet.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import java.util.List;

/**
 * @author khalidov
 * @version $Id$
 */
public class ObserverMetaProcessor extends SimpleProcessor {

    public ObserverMetaProcessor() {
        super(Observer.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        MetacodeContext context = ctx.metacodeContext;
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ObserverServant.ObserverMetacode.class), masterClassName));
        ClassName handlerClassName = ClassName.get(ObserverHandler.class);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyObservers")
                .addModifiers(Modifier.PUBLIC)
                .returns(handlerClassName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addParameter(Object.class, "observable");

        for (Element element : ctx.elements) {
            Observer annotation = element.getAnnotation(Observer.class);
            String observableClass = getOfClass(annotation);
            ClassName observableTypeName = ClassName.bestGuess(observableClass);
            ClassName metacodeTypeName = ClassName.bestGuess(MetacodeUtils.
                    getMetacodeOf(ctx.env.getElementUtils(), observableClass));

            List<? extends VariableElement> params = ((ExecutableElement) element).getParameters();
            if (params.size() != 1)
                throw new IllegalArgumentException("Observer method must have one parameter");
            TypeName eventTypeName = TypeName.get(params.get(0).asType());

            String methodHashName = ("getObservers" +
                    eventTypeName.toString().hashCode()).replace("-", "N");

            TypeSpec eventObserverTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(Observers.EventObserver.class), eventTypeName))
                    .addMethod(MethodSpec.methodBuilder("onEvent")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(eventTypeName, "event")
                            .returns(void.class)
                            .addStatement("master.$N(event)", element.getSimpleName().toString())
                            .build())
                    .build();

            methodBuilder
                    .beginControlFlow("if (observable.getClass() == $T.class)", observableTypeName)
                    .addStatement("$T handler = new $T()", handlerClassName, handlerClassName)
                    .addStatement("// hash of $S", eventTypeName.toString())
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

    private String getOfClass(Observer annotation) {
        String ofClass;
        try {
            ofClass = annotation.value().toString();

        } catch (MirroredTypesException e) {
            ofClass = e.getTypeMirrors().get(0).toString();
        }
        return ofClass;
    }
}