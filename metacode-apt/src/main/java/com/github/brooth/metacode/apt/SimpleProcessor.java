package com.github.brooth.metacode.apt;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public abstract class SimpleProcessor implements Processor {

    protected Class<? extends Annotation> annotation;
    @Nullable
    protected Class metacodeInterface;

    public SimpleProcessor(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    public SimpleProcessor(Class<? extends Annotation> annotation, @Nullable Class metacodeInterface) {
        this.annotation = annotation;
        this.metacodeInterface = metacodeInterface;
    }

    @Override
    public void collectElementsAnnotatedWith(Set<Class<? extends Annotation>> set) {
        set.add(annotation);
    }

    @Override
    public Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element) {
        return Collections.singleton(MetacodeUtils.typeOf(element));
    }

    @Nullable
    @Override
    public Set<String> masterMetacodeInterfaces(MetacodeContext ctx) {
        return metacodeInterface == null ? null : Collections.singleton(metacodeInterface.getCanonicalName());
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
