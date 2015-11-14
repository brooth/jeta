package com.github.brooth.metacode.apt;

import javax.annotation.processing.*;
import com.github.brooth.metacode.util.ImplementationServant;
import com.github.brooth.metacode.util.Implementation;
import com.squareup.javapoet.TypeSpec;

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
