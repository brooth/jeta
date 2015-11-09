package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.servants.MultitonServant;
import com.github.brooth.metacode.util.Multiton;

import java.util.List;

@Multiton(staticConstructor="newInstance")
public class HashMapMetasitory implements Metasitory {

	// todo use ClassForNameMetasitory in <init>(masterClass) 
	private static final MultitonServant multiton = new MultitonServant(
		new ClassForNameMetasitory(HashMapMetasitory.class), HashMapMetasitory.class);

  	public static HashMapMetasitory getInstance(String metapackage) {
		return multiton.getInstance(metapackage);	
	}
	
	static HashMapMetasitory newInstance(String metapackage) {
	  	//...
		return null;
	}

	@Override
	public List<MasterMetacode<?>> search(Criteria criteria) {
		return null;
	}
}

