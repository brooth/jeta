package org.javameta.apt;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Processor {

    /*
     * @return true if next round is needed
     */
    boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round);

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

    /*
     * No mater if master's source code hasn't been changed since its meta code generated,
     * return true to rebuild it
     */
    public boolean forceOverwriteMetacode();
    
	/**
	 * return true if current rounds set of annotations is needed in the next round
	 */
	public boolean needReclaim();
}
