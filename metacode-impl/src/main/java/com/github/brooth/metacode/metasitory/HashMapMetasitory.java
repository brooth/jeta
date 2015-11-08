package com.github.brooth.metacode.metasitory;

@Multiton(staticConstructor="newInstance")
public class HashMapMetasitory implements Metasitory {

	// todo use ClassForNameMetasitory in <init>(masterClass) 
	private static final MultitonServant multiton = new MultitonServant(
		new ClassForNameMetasitory(HasMapMetasitory.class), HasMapMetasitory.class);

  	public static HashMapMetasitory getInstance(String metapackage) {
		return multiton.getInstance(metapackage);	
	}
	
	static HashMapMetasitory newInstance(String metapackage) {
	  	//...
	}
}

