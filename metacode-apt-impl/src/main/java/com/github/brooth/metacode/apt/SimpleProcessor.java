package com.github.brooth.metacode.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

public abstract class SimpleProcessor implements Processor {

    protected Class<? extends Annotation> annotation;
    //@Nullable
    protected Class metacodeInterface;

    public SimpleProcessor(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    public SimpleProcessor(Class<? extends Annotation> annotation, Class metacodeInterface) {
        this.annotation = annotation;
        this.metacodeInterface = metacodeInterface;
    }

    public Class<? extends Annotation>[] collectElementWithAnnotations() {
        return new Class[]{annotation};
    }

    @Override
    public Collection<TypeElement> applicableMastersElements(ProcessingEnvironment env, Element element) {
        return Collections.singletonList(element.getKind().isClass() || element.getKind().isInterface()
                ? (TypeElement) element : (TypeElement) element.getEnclosingElement());
    }

    //@Nullable
    @Override
    public String[] metacodeInterfacesCodes(MetacodeContext ctx) {
        return metacodeInterface == null ? null : new String[]{metacodeInterface.getCanonicalName()};
    }

    @Override
    public boolean forceOverrideMetacode() {
        return false;
    }
}
