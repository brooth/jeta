package org.javameta.apt.processors;

import com.squareup.javapoet.TypeSpec;
import org.javameta.apt.ProcessorContext;
import org.javameta.util.Singleton;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonProcessor extends SimpleProcessor {

    public SingletonProcessor() {
        super(Singleton.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
