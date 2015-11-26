package org.javameta.apt.processors;

import com.squareup.javapoet.*;
import org.javameta.apt.MetacodeUtils;
import org.javameta.apt.ProcessorContext;
import org.javameta.collector.TypeCollector;
import org.javameta.collector.TypeCollectorMetacode;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class TypeCollectorProcessor extends SimpleProcessor {

    public TypeCollectorProcessor() {
        super(TypeCollector.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        // it's enough one element to collect the type
        final Element element = ctx.elements.iterator().next();
        builder.addSuperinterface(ClassName.get(TypeCollectorMetacode.class));

        TypeName annotationClassTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(Annotation.class));
        ParameterizedTypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
                ClassName.get(Class.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getTypeCollection")
                .addModifiers(Modifier.PUBLIC)
                .returns(listTypeName)
                .addParameter(annotationClassTypeName, "annotation");

        List<String> annotationsStr = MetacodeUtils.extractClassesNames(new Runnable() {
            @Override
            public void run() {
                element.getAnnotation(TypeCollector.class).value();
            }
        });
        for (String annotationStr : annotationsStr) {
            Set<? extends Element> annotatedElements =
                    roundEnv.getElementsAnnotatedWith(env.getElementUtils().getTypeElement(annotationStr));

            methodBuilder
                    .beginControlFlow("if(annotation == $L.class)", annotationStr)
                    .addStatement("$T result = new $T($L)", listTypeName, ParameterizedTypeName.get(
                            ClassName.get(ArrayList.class), ClassName.get(Class.class)), annotatedElements.size());

            for (Element annotatedElement : annotatedElements) {
                methodBuilder.addStatement("result.add($L.class)",
                        MetacodeUtils.typeElementOf(annotatedElement).toString());
            }

            methodBuilder
                    .addStatement("return result")
                    .endControlFlow();
        }

        methodBuilder.addStatement("throw new IllegalArgumentException(getMasterClass() + \" doesn't collect types of \" + annotation)");
        builder.addMethod(methodBuilder.build());
        return false;
    }
}
