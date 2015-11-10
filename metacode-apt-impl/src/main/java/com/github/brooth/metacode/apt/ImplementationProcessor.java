package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.servants.ImplementationServant;
import com.github.brooth.metacode.util.Implementation;
import com.squareup.javawriter.JavaWriter;

/**
 *
 */
public class ImplementationProcessor extends SimpleProcessor {

    public ImplementationProcessor() {
        super(Implementation.class, ImplementationServant.ImplementationMetacode.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, JavaWriter out, int round) {
        return false;
    }
}
