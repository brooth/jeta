package com.github.brooth.metacode.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;

public final class ProcessorContext {
	public ProcessingEnvironment env;
	public List<Element> elements;
	public MetacodeContext metacodeContext;
}
