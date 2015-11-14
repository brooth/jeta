package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.observer.*;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.Collections;

/**
 * 
 */
public class ObservableProcessor extends SimpleProcessor {

	public ObservableProcessor() {
    	super(Subject.class);
	}

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
		MetacodeContext context = ctx.metacodeContext;
		ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
			ClassName.get(ObservableServant.ObservableMetacode.class), masterClassName));

		MethodSpec.Builder applyMethodSpecBuilder = MethodSpec.methodBuilder("applyObservable")
		    .addModifiers(Modifier.PUBLIC)
		    .returns(void.class)
		    .addParameter(masterClassName, "master");

		for(Element element : ctx.elements) {
			TypeName observersTypeName = TypeName.get(element.asType());
			TypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), 
				masterClassName, observersTypeName);

			String fieldName = element.getSimpleName().toString();
			FieldSpec observersField = FieldSpec.builder(mapTypeName, fieldName)
    			.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
    			.initializer("new $T<>()", WeakHashMap.class)
    			.build();
			builder.addField(observersField);

			String eventTypeStr = observersTypeName.toString();
			int i = eventTypeStr.indexOf('<');
            if(i == -1)
				throw new IllegalArgumentException("Not valid @Subject usage, define event type as generic");

			eventTypeStr = eventTypeStr.substring(i + 1, eventTypeStr.lastIndexOf('>'));
			String methodHashName = ("getObservers" + eventTypeStr.hashCode()).replace("-", "N");
			
			MethodSpec getObserversMethodSpec = MethodSpec.methodBuilder(methodHashName)
			    .addJavadoc("hash of $S\n", eventTypeStr.toString())
				.addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
			    .returns(observersTypeName)
				.addParameter(masterClassName, "master")
      			.addStatement("$T result = $L.get(master)", observersTypeName, fieldName)
				.beginControlFlow("if (result == null)")
      			.addStatement("result = new $T()", observersTypeName)
      			.addStatement("$L.put(master, result)", fieldName)
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
