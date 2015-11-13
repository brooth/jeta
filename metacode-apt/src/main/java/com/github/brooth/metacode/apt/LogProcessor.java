package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.log.Log;
import javax.annotation.processing.*;
import com.github.brooth.metacode.log.LogServant;
import com.squareup.javapoet.TypeSpec;

/**
 * -AmcLoggerMethodFormat="setName(\"%s\")"
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class, LogServant.LogMetacode.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec masterType, int round) {
        return false;
    }
}
