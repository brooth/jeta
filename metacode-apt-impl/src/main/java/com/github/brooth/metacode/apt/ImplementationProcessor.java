package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.util.Implementation;

/**
 *
 */
public class ImplementationProcessor extends SimpleProcessor {

    public ImplementationProcessor() {
        super(Implementation.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, int round) {
        return false;
    }
}
