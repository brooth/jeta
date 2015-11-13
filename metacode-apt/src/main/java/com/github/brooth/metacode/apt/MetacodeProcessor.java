package com.github.brooth.metacode.apt;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * -AmcAdd={com.example.apt.MyCustomProcessor}   - add processor
 * -AmcExclude={Inject.*, LogProcessor}          - exclude processors
 * -AmcMetasitory=com.example.MyMetasitory       - set metasitory
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MetacodeProcessor extends AbstractProcessor {

    private List<Processor> processors = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "init");

        processors.add(new ObserverProcessor());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


        return false;
    }
}
