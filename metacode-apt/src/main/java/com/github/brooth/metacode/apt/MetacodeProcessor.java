package com.github.brooth.metacode.apt;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * -AmcAdd={com.example.apt.MyCustomProcessor}   - add processor
 * -AmcExclude={Inject.*, LogProcessor}          - exclude processors
 * -AmcMetasitory=com.example.MyMetasitory       - set metasitory
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MetacodeProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
