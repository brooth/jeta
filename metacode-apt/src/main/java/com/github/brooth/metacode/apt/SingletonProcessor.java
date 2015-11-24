package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.util.Singleton;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.RoundEnvironment;

/**
 * 
 */
public class SingletonProcessor extends SimpleProcessor {

    public SingletonProcessor() {
        super(Singleton.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
