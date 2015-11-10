package com.github.brooth.metacode.apt;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public List<? extends Class<? extends Annotation>> collectElementsAnnotatedWith() {
        return Collections.singletonList(annotation);
    }

    @Override
    public Collection<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element) {
        return Collections.singletonList(element.getKind().isClass() || element.getKind().isInterface()
                ? (TypeElement) element : (TypeElement) element.getEnclosingElement());
    }

    @Nullable
    @Override
    public String[] masterMetacodeInterfaces(MetacodeContext ctx) {
        return metacodeInterface == null ? null : new String[]{metacodeInterface.getCanonicalName()};
    }

    @Override
    public boolean forceOverwriteMetacode() {
        return false;
    }
}
