package com.github.brooth.metacode.metasitory;

public class ClassForNameMetasitory implements Metasitory {

	private Class metacodeClass;

  	public ClassForNameMetasitory(Class masterClass) {
		metacodeClass = Class.forName(masterClass.getName() + "_Metacode");	
	}
	
	@Override
	public List<MasterClass> search(Criteria c) {
	  	return Collections.singleList(masterClass.newInstance());
	}
}

