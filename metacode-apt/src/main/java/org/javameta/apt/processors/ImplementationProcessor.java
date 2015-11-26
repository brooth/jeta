package org.javameta.apt.processors;

import com.squareup.javapoet.TypeSpec;
import org.javameta.apt.ProcessorContext;
import org.javameta.util.Implementation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 *
 */
public class ImplementationProcessor extends SimpleProcessor {

    public ImplementationProcessor() {
        super(Implementation.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
