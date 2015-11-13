package com.github.brooth.metacode.apt;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Processor {

    /*
     * @return true if next round is needed
     */
    boolean process(ProcessorContext ctx, TypeSpec masterType, int round);

    /*
     * Tell to MetacodeProcessor the annotations, it should collect elements with.
     * All the elements will passed to this processor in generating metacode stage.
     */
    public void collectElementsAnnotatedWith(Set<Class<? extends Annotation>> set) ;

    /*
     * Ensure type elements (masters elements) associated with @param element
     * For those elements metacode will be generated.
     */
    Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element);

    /**
     * Java code of interfaces, master's metacode will be implementation of.
     * @deprecated add in type itself
     */
    @Deprecated
    Set<String> masterMetacodeInterfaces(MetacodeContext ctx);

    /*
     * No mater if master's source code hasn't been changed since its meta code generated,
     * return true to rebuild it
     */
    public boolean forceOverwriteMetacode();

    public static class ProcessorContext {
        public List<Element> elements;
        public ProcessingEnvironment env;
        public RoundEnvironment roundEnv;
        public MetacodeContext metacodeContext;
    }
}
