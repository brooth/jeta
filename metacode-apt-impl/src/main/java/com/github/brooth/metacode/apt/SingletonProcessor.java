package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.util.Singleton;

/**
 * 
 */
public class SingletonProcessor extends SimpleProcessor {

    public SingletonProcessor() {
        super(Singleton.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, int round) {
        return false;
    }
}
