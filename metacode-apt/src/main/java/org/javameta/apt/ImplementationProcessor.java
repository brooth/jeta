package org.javameta.apt;

import com.squareup.javapoet.TypeSpec;
import org.javameta.util.Implementation;

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
