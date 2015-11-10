package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.util.Singleton;
import com.squareup.javawriter.JavaWriter;

/**
 * 
 */
public class SingletonProcessor extends SimpleProcessor {

    public SingletonProcessor() {
        super(Singleton.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, JavaWriter out, int round) {
        return false;
    }
}
