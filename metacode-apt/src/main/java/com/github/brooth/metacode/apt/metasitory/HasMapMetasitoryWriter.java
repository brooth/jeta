package com.github.brooth.metacode.apt.metasitory;

import com.github.brooth.metacode.apt.MetacodeContext;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * -AmcMetasitoryPackage=com.example             - metasitory package
 * -AmcSuperMetasitoryPackage=com.example        - super metasitory package. ???
 */
public class HasMapMetasitoryWriter implements MetasitoryWriter {
    /**
     * 	map.put(MasterA.class, new Ctx(MasterA_Metacode.class,
     * 		new Provider<MasterA>() {
     * 		  	public MasterA get() {
     * 				return new MasterA_Metacode();
     * 			}
     * 		},
     * 		Meta.class, Log.class);
     */

    @Override
    public void open(ProcessingEnvironment env) {
    }

    @Override
    public void write(MetacodeContext context) {
    }

    @Override
    public void close() {
    }
}
