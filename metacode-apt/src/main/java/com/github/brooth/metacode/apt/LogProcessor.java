package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.servants.LogServant;
import com.squareup.javapoet.TypeSpec;

/**
 * -AmcLoggerMethodFormat="setName(\"%s\")"
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class, LogServant.LogMetacode.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, TypeSpec masterType, int round) {
        return false;
    }
}
