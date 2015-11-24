package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.log.Log;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.RoundEnvironment;

/**
 * -AmcLoggerMethodFormat="setName(\"%s\")"
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
