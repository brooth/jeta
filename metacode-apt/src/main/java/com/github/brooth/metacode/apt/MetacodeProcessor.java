package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.apt.metasitory.HasMapMetasitoryWriter;
import com.github.brooth.metacode.apt.metasitory.MetasitoryWriter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * -AmcAdd={com.example.apt.MyCustomProcessor}   - add processor
 * -AmcExclude={Inject.*, LogProcessor}          - exclude processors
 * -AmcMetasitory=com.example.MyMetasitory       - set metasitory
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MetacodeProcessor extends AbstractProcessor {

    public static final String METACODE_CLASS_POSTFIX = "_Metacode";

    private ProcessingEnvironment env;
    private Messager messager;
    private Elements elementsUtils;

    private List<Processor> processors = new ArrayList<>();
    private List<MetacodeContextImpl> metacodeContextList = new ArrayList<>(500);
    private int round = -1;
    private int blankRounds = 0;
    private MetasitoryWriter metasitoryWriter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "init");

        this.env = processingEnv;
        this.elementsUtils = processingEnv.getElementUtils();

        metasitoryWriter = new HasMapMetasitoryWriter();

        processors.add(new ObserverProcessor());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "processing. round: " + (++round));

        if (round == 0) {
            metacodeContextList.clear();
            assembleMetacodeContextList(roundEnv);
        }

        if (!metacodeContextList.isEmpty()) {
            if (round == 0) {
                metasitoryWriter.open(env);
                generateMetaCodes();
            }

            return processAutoCodes(roundEnv);
        }

        if (blankRounds == 2) {
            metasitoryWriter.close();
        }

        blankRounds++;
        return false;
    }

    private void generateMetaCodes() {

    }

    private boolean processAutoCodes(RoundEnvironment roundEnv) {
        return false;
    }

    private void assembleMetacodeContextList(RoundEnvironment roundEnv) {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(5);
        for (final Processor processor : processors) {
            annotations.clear();
            processor.collectElementsAnnotatedWith(annotations);
            if (annotations.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Processor " + processor.getClass().getCanonicalName()
                        + " without annotations. Nothing to collect");
                continue;
            }

            // go through processor's annotations
            for (Class<? extends Annotation> annotation : annotations) {
                Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
                if (elements.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "No elements found with @" + annotation);
                    continue;
                }

                // go through annotated elements
                for (Element element : elements) {
                    // go through element's masters
                    for (TypeElement masterTypeElement : processor.applicableMastersOfElement(env, element)) {
                        final String masterCanonicalName = masterTypeElement.toString();
                        MetacodeContextImpl context = Iterables.find(metacodeContextList,
                                new MasterCanonicalNamePredicate(masterCanonicalName), new MetacodeContextImpl(masterTypeElement));
                        assert context != null;

                        Processor.ProcessorContext processorContext = context.processorContextMap.get(processor);
                        if (processorContext == null) {
                            processorContext = new Processor.ProcessorContext();
                            processorContext.env = env;
                            processorContext.metacodeContext = context;
                            processorContext.elements = new ArrayList<>();
                            context.processorContextMap.put(processor, processorContext);
                        }
                        processorContext.elements.add(element);
                    }
                }
            }
        }
    }

    private static class MetacodeContextImpl implements MetacodeContext {

        // JavaWriter writer();
        private Map<Processor, Processor.ProcessorContext> processorContextMap;

        public MetacodeContextImpl(TypeElement masterTypeElement) {

        }

        @Override
        public String getMasterPackage() {
            return null;
        }

        @Override
        public String getMasterCanonicalName() {
            return null;
        }

        @Override
        public String getMasterSimpleName() {
            return null;
        }

        @Override
        public String getMetacodeSimpleName() {
            return null;
        }

        @Override
        public String getMasterFlatName() {
            return null;
        }

        @Override
        public String getSourceCanonicalName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> metacodeAnnotations() {
            return null;
        }
    }

    private static class MasterCanonicalNamePredicate implements Predicate<MetacodeContext> {
        private final String masterCanonicalName;

        public MasterCanonicalNamePredicate(String masterCanonicalName) {
            this.masterCanonicalName = masterCanonicalName;
        }

        @Override
        public boolean apply(MetacodeContext input) {
            return input.getMasterCanonicalName().equals(masterCanonicalName);
        }
    }
}
