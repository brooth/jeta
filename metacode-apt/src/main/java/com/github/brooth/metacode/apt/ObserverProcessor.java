package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.observer.Observer;
import com.github.brooth.metacode.observer.Subject;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author khalidov
 * @version $Id$
 */
public class ObserverProcessor implements Processor {

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec masterType, int round) {
        return false;
    }

    @Override
    public void collectElementsAnnotatedWith(Set<Class<? extends Annotation>> set) {
        set.add(Subject.class);
        set.add(Observer.class);
    }

    @Override
    public Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element) {
        return Collections.singleton(MetacodeUtils.typeOf(element));
    }

    @Nullable
    @Override
    public Set<String> masterMetacodeInterfaces(MetacodeContext ctx) {
        Set<String> set = new HashSet<>();
        if (ctx.metacodeAnnotations().contains(Subject.class))
            set.add("com.github.brooth.metacode.observer.ObservableServant.Observable");
        if (ctx.metacodeAnnotations().contains(Observer.class))
            set.add("com.github.brooth.metacode.observer.ObserverServant.Observer<" + ctx.getMasterCanonicalName() + '>');
        return set;
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
