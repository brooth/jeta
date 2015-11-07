package com.github.brooth.metacode.apt;

public interface Processor {

	/*
	 * Tell to MetacodeProcessor the annotations, it should collect elements with.
	 * All the elements will passed to this processor in generating metacode stage.
	 */
	Class<? extends Annotation>[] collectElementWithAnnotations();

	/*
	 * Ensure type elements (masters elements) associated with @param element
	 * For those elements metacode will be generated.
	 */
	Collection<TypeElement> applicableMasters(ProcessingEnvironment env, Element element); 

	/*
	 * Java code of iterfaces, master's metacode should implement. 
	 */
	@Nullable
	String[] metacodeInterfaces(MetacodeContext ctx);

	/*
	 * No mater if master's source code hasn't been changed since its meta code generated,
	 * return true to rebuild it again
	 */
	public boolean forceOverrideMetacode();

	/**
	 * @return true if need next round
	 */
	boolean process(ProcessorContext ctx, JavaWriver out, int round);

	public static class ProcessorContext {
		public List<Element> elements;
		public ProcessingEnvironment env;
		public RoundEnvironment roundEnv;
		public MetacodeContext metacodeContext;
	}
}
