package com.github.brooth.metacode.apt;

import com.squareup.javawriter.JavaWriter;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public interface Processor {

	/*
	 * Tell to MetacodeProcessor the annotations, it should collect elements with.
	 * All the elements will passed to this processor in generating metacode stage.
	 */
	List<? extends Class<? extends Annotation>> collectElementsAnnotatedWith();

	/*
	 * Ensure type elements (masters elements) associated with @param element
	 * For those elements metacode will be generated.
	 */
	Collection<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element);

	/*
	 * Java code of interfaces, master's metacode will be implementation of.
	 */
	@Nullable
	String[] masterMetacodeInterfaces(MetacodeContext ctx);

	/*
	 * No mater if master's source code hasn't been changed since its meta code generated,
	 * return true to rebuild it 
	 */
	public boolean forceOverwriteMetacode();

	/*
	 * @return true if next round is needed
	 */
	boolean process(ProcessorContext ctx, JavaWriter out, int round);

	public static class ProcessorContext {
		public List<Element> elements;
		public ProcessingEnvironment env;
		public RoundEnvironment roundEnv;
		public MetacodeContext metacodeContext;
	}
}
