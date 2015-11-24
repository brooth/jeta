package com.github.brooth.metacode.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public abstract class SimpleProcessor implements Processor {

    protected Class<? extends Annotation> annotation;

    public SimpleProcessor(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public void collectElementsAnnotatedWith(Set<Class<? extends Annotation>> set) {
        set.add(annotation);
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
