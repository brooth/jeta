package com.github.brooth.metacode.apt.metasitory;

import com.github.brooth.metacode.apt.MetacodeContext;

import javax.annotation.processing.*;
import javax.tools.Diagnostic;

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

    private Messager messager;

    @Override
    public void open(ProcessingEnvironment env) {
        messager = env.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "open metasitory");
    }

    @Override
    public void write(MetacodeContext context) {
        messager.printMessage(Diagnostic.Kind.NOTE, "write in metasitory");
    }

    @Override
    public void close() {
        messager.printMessage(Diagnostic.Kind.NOTE, "close metasitory");
    }
}
