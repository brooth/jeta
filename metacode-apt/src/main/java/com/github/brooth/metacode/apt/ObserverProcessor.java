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

		for(Element element : ctx.elements) {
			TypeName eventTypeName = TypeName.get(element.asType());
			TypeName observersTypeName = ParameterizedTypeName.get(
				ClassName.get(Observers.class), eventTypeName); 
			TypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), 
				masterClassName, observersTypeName);

			FieldSpec observersField = FieldSpec.builder(mapTypeName, element.getSimpleName().toString())
    			.addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
    			.initializer("new $T<>()", WeakHashMap.class)
    			.build();
			builder.addField(observersField);
		}

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
