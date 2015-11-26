package org.javameta.apt.processors;

import com.squareup.javapoet.TypeSpec;
import org.javameta.apt.ProcessorContext;
import org.javameta.log.Log;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 * -AmcLoggerMethodFormat="setName(\"%s\")"
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
