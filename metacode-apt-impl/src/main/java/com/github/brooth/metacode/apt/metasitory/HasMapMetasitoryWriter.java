package com.github.brooth.metacode.apt.metasitory;

/**
 *
 * -AmcMetasitoryPackage=com.example             - metasitory package   
 */
public class HasMapMetasitoryWriter implements MetasitoryWriter {
    
	@Override
	public void open(ProcessingEnv env) {
	}
    
	@Override
	public void write(Record r) {
	}
    
	@Override
	public void close() {
	}
}


"	map.put(MasterA.class, new Ctx(MasterA_Metacode.class, 
"		new Provider<MasterA>() {
"		  	public MasterA get() {
"				return new MasterA_Metacode();
"			}
"		},
"		Meta.class, Log.class);
