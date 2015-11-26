package org.javameta.apt;

import javax.lang.model.element.Element;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public final class ProcessorContext {
	public List<Element> elements;
	public MetacodeContext metacodeContext;
	public Logger logger;
}
