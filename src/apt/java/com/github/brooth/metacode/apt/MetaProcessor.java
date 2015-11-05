package com.github.brooth.metacode.apt;

public abstract class MetaProcessor {
	
	private Class<? extends Annotation>[] annotations;

	public MetaProcessor(Class<? extends Annotation>... searchAnnotations) {
		this.annotations = searchAnnotations;	
	}

	/**
	 * @return true if need next round
	 */
	protected boolean process(Context ctx, JavaWriver out, int round);

	@Nullable
	public abstract String[] emitMetacodeInterfaces() {
		// go throught annotations
	}

	public boolean needOverrideMetacode() {
		return false;
	}

	public static class Context {
		List<Element> elements;
		MetacodeContext context;
	}
}
