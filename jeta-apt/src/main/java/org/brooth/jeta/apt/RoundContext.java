package org.brooth.jeta.apt;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.util.Collection;

public interface RoundContext {

	Collection<Element> elements();

	MetacodeContext metacodeContext();
	
	RoundEnvironment roundEnv(); 
	
	int round();
}
