package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.pubsub.Publish;
import com.github.brooth.metacode.pubsub.PublisherMetacode;
import com.squareup.javapoet.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 *
 */
public class PublishProcessor extends SimpleProcessor {

    public PublishProcessor() {
        super(Publish.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        MetacodeContext context = ctx.metacodeContext;
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(PublisherMetacode.class), masterClassName));

        MethodSpec.Builder applyMethodSpecBuilder = MethodSpec.methodBuilder("applyPublisher")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(masterClassName, "master");

        for (Element element : ctx.elements) {
            TypeName subscribersTypeName = TypeName.get(element.asType());
            String fieldName = element.getSimpleName().toString();
            FieldSpec observersField = FieldSpec.builder(subscribersTypeName, fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .build();
            builder.addField(observersField);

            String eventTypeStr = subscribersTypeName.toString();
            int i = eventTypeStr.indexOf('<');
            if (i == -1)
                throw new IllegalArgumentException("Not valid @Publish usage, define event type as generic of Subscribers");

            eventTypeStr = eventTypeStr.substring(i + 1, eventTypeStr.lastIndexOf('>'));
            String methodHashName = ("getSubscribers" + eventTypeStr.hashCode()).replace("-", "N");

            MethodSpec getObserversMethodSpec = MethodSpec.methodBuilder(methodHashName)
                    .addJavadoc("hash of $S\n", eventTypeStr)
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .returns(subscribersTypeName)
                    .beginControlFlow("if ($L == null)", fieldName)
                    .addStatement("$L = new $T()", fieldName, subscribersTypeName)
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
