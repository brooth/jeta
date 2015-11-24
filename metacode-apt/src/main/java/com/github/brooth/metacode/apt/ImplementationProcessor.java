package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.util.Implementation;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.RoundEnvironment;

/**
 *
 */
public class ImplementationProcessor extends SimpleProcessor {

    public ImplementationProcessor() {
        super(Implementation.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
