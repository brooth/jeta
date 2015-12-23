package org.brooth.jeta.apt;

import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public interface RoundContext {

	List<Element> elements();

	MetacodeContext metacodeContext();
	
	RoundEnvironment roundEnv(); 
	
	int round();
}
