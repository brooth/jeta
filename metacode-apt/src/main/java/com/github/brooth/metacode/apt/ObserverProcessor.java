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
 * @author khalidov
 * @version $Id$
 */
public class ObserverProcessor implements Processor {

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

			String methodHashName = ("getObservers" + 
				observersTypeName.toString().hashCode()).replace("-", "N");
			
			MethodSpec getObserversMethodSpec = MethodSpec.methodBuilder(methodHashName)
			    .addJavadoc("method name hash of $S\n", observersTypeName.toString())
				.addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
			    .returns(observersTypeName)
				.addParameter(masterClassName, "master")
				.beginControlFlow("if (!$L.containsKey(master))", fieldName)
      			.addStatement("$L.put(master, new $T())", fieldName, observersTypeName)
      			.endControlFlow()
      			.addStatement("return $L.get(master)", fieldName)
				.build();
			builder.addMethod(getObserversMethodSpec);

			applyMethodSpecBuilder.addStatement("master.$L = $L(master)", fieldName, methodHashName);
		}
 		builder.addMethod(applyMethodSpecBuilder.build());

        return false;
    }

    @Override
    public void collectElementsAnnotatedWith(Set<Class<? extends Annotation>> set) {
        set.add(Subject.class);
     //   set.add(Observer.class);
    }

    @Override
    public Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element) {
        return Collections.singleton(MetacodeUtils.typeOf(element));
    }

    @Override
	public boolean needReclaim() {
		return false;
	}

    @Override
    public boolean forceOverwriteMetacode() {
        return false;
    }
}
