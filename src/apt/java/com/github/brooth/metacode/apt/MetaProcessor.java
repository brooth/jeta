package com.github.brooth.metacode.apt;

public abstract class BaseProcessor {
	
	protected Class<? extends Annotation>[] annotations;

	public BaseProcessor(Class<? extends Annotation>... searchAnnotations) {
		this.annotations = searchAnnotations;	
	}

	/**
	 * @return true if need next round
	 */
	protected boolean process(ProcessorContext ctx, JavaWriver out, int round);

	protected Collection<Element> getApplicableMasters(ProcessingEnvironment env, Element element) {                                                                               
		return Collections.singletonList(element.getKind().isClass() || element.getKind().isInterface() 
			? element : element.getEnclosingElement());
	}

	@Nullable
	public abstract String[] emitMetacodeInterfaces(MetacodeContext ctx) {
		// go throught annotations
	}

	public boolean forceOverrideMetacode() {
		return false;
	}

	public static class ProcessorContext {
		public List<Element> elements;
		public ProcessingEnvironment env;
		public RoundEnvironment roundEnv;
		public MetacodeContext context;
	}
}
