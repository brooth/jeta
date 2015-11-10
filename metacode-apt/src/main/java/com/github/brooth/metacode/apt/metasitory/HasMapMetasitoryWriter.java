package com.github.brooth.metacode.apt.metasitory;

import javax.annotation.processing.ProcessingEnvironment;

/**
 *
 * -AmcMetasitoryPackage=com.example             - metasitory package   
 */
public class HasMapMetasitoryWriter implements MetasitoryWriter {
	/**
	 "	map.put(MasterA.class, new Ctx(MasterA_Metacode.class,
	 "		new Provider<MasterA>() {
	 "		  	public MasterA get() {
	 "				return new MasterA_Metacode();
	 "			}
	 "		},
	 "		Meta.class, Log.class);
	 **/

	@Override
	public void open(ProcessingEnvironment env) {
	}
    
	@Override
	public void write(Record r) {
	}
    
	@Override
	public void close() {
	}
}
