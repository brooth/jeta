package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.servants.LogServant;
import com.squareup.javawriter.JavaWriter;

/**
 * -AmcLoggerMethodFormat="setName(\"%s\")"
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class, LogServant.LogMetacode.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, JavaWriter out, int round) {
        return false;
    }
}
