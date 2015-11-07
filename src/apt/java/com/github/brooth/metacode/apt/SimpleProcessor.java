package com.github.brooth.metacode.apt;

public abstract class SimpleProcessor implements Processor {
	
	protected Class<? extends Annotation> annotation;
	@Nullable
	protected Class metacodeInterface;

	public SimpleProcessor(Class<? extends Annotation> annotation) {
		this.annotation = annotation;	
	}

	public SimpleProcessor(Class<? extends Annotation> annotation, Class metacodeInterface) {
		this.annotation = annotation;	
		this.metacodeInterface = metacodeInterface;
	}

	public Class<? extends Annotation>[] collectElementWithAnnotations() {
		return new Class<? extends Annotation>[] {annotation};
	}

	@Override
	public Collection<TypeElement> applicableMasters(ProcessingEnvironment env, Element element); 
		return Collections.singletonList(element.getKind().isClass() || element.getKind().isInterface() 
			? element : element.getEnclosingElement());
	}

	@Nullable
	@Override
	public String[] metacodeInterfaces(MetacodeContext ctx) {
		return metacodeInterface == null ? null : new String[] {metacodeInterface.getCanonicalName()};
	}

	@Override
	public boolean forceOverrideMetacode() {
		return false;
	}
}
