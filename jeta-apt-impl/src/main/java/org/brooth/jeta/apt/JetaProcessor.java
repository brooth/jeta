/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brooth.jeta.apt;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.*;
import org.brooth.jeta.IMetacode;
import org.brooth.jeta.apt.metasitory.MapMetasitoryWriter;
import org.brooth.jeta.apt.metasitory.MetasitoryEnvironment;
import org.brooth.jeta.apt.metasitory.MetasitoryWriter;
import org.brooth.jeta.apt.processors.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * jetaDebug=true                                - debug output
 * jetaMetasitory=com.example.MyMetasitory       - set metasitory
 * jetaAdd={com.example.apt.MyCustomProcessor}   - add processor
 * jetaDisable=Meta.*,Log                        - exclude processors
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JetaProcessor extends AbstractProcessor {

    public static final String METACODE_CLASS_POSTFIX = "_Metacode";

    private ProcessingEnvironment env;
    private Messager logger;
    private Elements elementUtils;

    private List<Processor> processors = new ArrayList<>();
    private List<MetacodeContextImpl> metacodeContextList = new ArrayList<>(500);

    private MetasitoryWriter metasitoryWriter;

    private int round = 0;
    private int blankRounds = 0;
    private long ts;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.env = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();

        logger = new Messager();
        logger.messager = processingEnv.getMessager();
        logger.debug = Boolean.parseBoolean(Optional.fromNullable(env.getOptions().get("jetaDebug")).or("false"));
        logger.debug("init");

        addProcessors();
    }

    protected void addProcessors() {
        processors.add(new ObservableProcessor());
        processors.add(new ObserverProcessor());
        processors.add(new ProxyProcessor());
        processors.add(new PublishProcessor());
        processors.add(new SubscribeProcessor());
        processors.add(new ValidateProcessor());
        processors.add(new TypeCollectorProcessor());
        processors.add(new ObjectCollectorProcessor());
        processors.add(new LogProcessor());
        processors.add(new SingletonProcessor());
        processors.add(new MultitonProcessor());
        processors.add(new ImplementationProcessor());
        processors.add(new MetaProcessor());
        processors.add(new MetaEntityProcessor());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.debug("processing. round: " + (++round));

        if (round == 1) {
            ts = System.currentTimeMillis();
            metacodeContextList.clear();
            removeDisabledProcessors();
            assembleMetacodeContextList(roundEnv);
        }

        if (!metacodeContextList.isEmpty()) {
            if (round == 1) {
                metasitoryWriter = new MapMetasitoryWriter();
                MetasitoryEnvironmentImpl impl = new MetasitoryEnvironmentImpl();
                impl.env = env;
                impl.logger = logger;
                metasitoryWriter.open(impl);
                generateMetaTypeBuilders();
            }

            return !processMetacodes(roundEnv);
        }

        if (round > 1 && blankRounds == 0) {
            metasitoryWriter.close();
        }

        if (blankRounds == 1) {
            logger.debug(String.format("built in %dms", System.currentTimeMillis() - ts));
        }

        blankRounds++;
        return true;
    }

    protected void removeDisabledProcessors() {
        Iterator<Processor> iter = processors.iterator();
        while (iter.hasNext()) {
            Processor processor = iter.next();
            if (!processor.isEnabled(processingEnv)) {
                logger.debug("processor " + processor.getClass().getCanonicalName() + " disabled");
                iter.remove();
            }
        }
    }

    private void generateMetaTypeBuilders() {
        logger.debug("generating meta type builders");

        for (MetacodeContextImpl context : metacodeContextList) {
            logger.debug("    + " + context.metacodeCanonicalName);

            ClassName masterClassName = ClassName.get(context.masterElement);
            TypeSpec.Builder builder = TypeSpec.classBuilder(context.metacodeSimpleName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addJavadoc("Generated by Jeta, http://brooth.jeta.org\n")
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(IMetacode.class), masterClassName));

            builder.addMethod(MethodSpec.methodBuilder("getMasterClass")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), masterClassName))
                    .addStatement("return $T.class", masterClassName)
                    .build());

            context.builder = builder;
        }
    }

    private boolean processMetacodes(RoundEnvironment roundEnv) {
        boolean reclaim = false;
        Iterator<MetacodeContextImpl> iterator = metacodeContextList.iterator();
        while (iterator.hasNext()) {
            MetacodeContextImpl context = iterator.next();
            Iterator<Map.Entry<Processor, ProcessorEnvironmentImpl>> processorEnvIterator
                    = context.processorEnvironments.entrySet().iterator();
            while (processorEnvIterator.hasNext()) {
                Map.Entry<Processor, ProcessorEnvironmentImpl> entry = processorEnvIterator.next();
                Processor processor = entry.getKey();
                ProcessorEnvironmentImpl processorEnvironment = entry.getValue();

                logger.debug("processing " + context.metacodeCanonicalName +
                        " with " + processor.getClass().getSimpleName());

                processorEnvironment.roundEnvironment = roundEnv;
                processorEnvironment.round = round;
                if (!processor.process(processorEnvironment, context.builder))
                    processorEnvIterator.remove();

                if (processor.needReclaim())
                    reclaim = true;
            }

            if (context.processorEnvironments.isEmpty()) {
                String pkg = env.getElementUtils().getPackageOf(context.masterElement).getQualifiedName().toString();
                JavaFile.Builder fileBuilder = JavaFile.builder(pkg, context.builder.build()).indent("\t");
                if (env.getOptions().containsKey("jetaFileComment"))
                    fileBuilder.addFileComment(env.getOptions().get("jetaFileComment"));
                JavaFile javaFile = fileBuilder.build();
                Writer out = null;
                try {
                    JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(context.metacodeCanonicalName);
                    out = sourceFile.openWriter();
                    javaFile.writeTo(out);
                    out.close();

                } catch (IOException e) {
                    throw new RuntimeException("failed to write metacode file", e);

                } finally {
                    if (out != null)
                        try {
                            out.close();

                        } catch (IOException e) {
                            // os corrupted successfully
                        }
                }

                metasitoryWriter.write(context);

                iterator.remove();
                logger.debug("generating " + context.metacodeCanonicalName + " complete");
            }
        }
        return reclaim;
    }

    private void assembleMetacodeContextList(RoundEnvironment roundEnv) {
        logger.debug("assemble metacode context list");

        for (final Processor processor : processors) {
            Set<Class<? extends Annotation>> annotations = processor.collectElementsAnnotatedWith();
            if (annotations.isEmpty()) {
                logger.warn("Processor " + processor.getClass().getCanonicalName()
                        + " without annotations. Nothing to collect");
                continue;
            }

            // go through processor's annotations
            for (Class<? extends Annotation> annotation : annotations) {
                logger.debug("collecting elements with @" + annotation.getSimpleName());

                Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
                if (elements.isEmpty()) {
                    logger.debug("no elements found with @" + annotation.getSimpleName());
                    continue;
                }

                // go through annotated elements
                for (Element element : elements) {
                    logger.debug("found element: " + element.toString());

                    // go through element's masters
                    for (TypeElement masterTypeElement : processor.applicableMastersOfElement(env, element)) {
                        logger.debug("applicable master: " + masterTypeElement.toString());

                        MetacodeContextImpl context = Iterables.find(metacodeContextList,
                                new MasterTypePredicate(masterTypeElement), null);
                        if (context == null) {
                            context = new MetacodeContextImpl(elementUtils, masterTypeElement);
                            metacodeContextList.add(context);

                            logger.debug("    masterCanonicalName   - " + context.masterElement.toString());
                            logger.debug("    metacodeCanonicalName - " + context.metacodeCanonicalName);
                        }
                        context.metacodeAnnotations().add(annotation);

                        ProcessorEnvironmentImpl processorEnvironment = context.processorEnvironments.get(processor);
                        if (processorEnvironment == null) {
                            processorEnvironment = new ProcessorEnvironmentImpl();
                            processorEnvironment.processingEnvironment = env;
                            processorEnvironment.metacodeContext = context;
                            processorEnvironment.elements = new ArrayList<>();
                            processorEnvironment.logger = logger;
                            context.processorEnvironments.put(processor, processorEnvironment);
                        }
                        processorEnvironment.elements.add(element);
                    }
                }
            }
        }
    }

    private static class ProcessorEnvironmentImpl implements ProcessorEnvironment {
        private ProcessingEnvironment processingEnvironment;
        private RoundEnvironment roundEnvironment;
        private MetacodeContext metacodeContext;
        private List<Element> elements;
        private Messager logger;
        private int round;

        @Override
        public ProcessingEnvironment processingEnv() {
            return processingEnvironment;
        }

        @Override
        public RoundEnvironment roundEnv() {
            return roundEnvironment;
        }

        @Override
        public List<Element> elements() {
            return elements;
        }

        @Override
        public MetacodeContext metacodeContext() {
            return metacodeContext;
        }

        @Override
        public Logger logger() {
            return logger;
        }

        @Override
        public int round() {
            return round;
        }
    }

    private static class MetacodeContextImpl implements MetacodeContext {
        private TypeSpec.Builder builder;
        private Map<Processor, ProcessorEnvironmentImpl> processorEnvironments = new HashMap<>();

        private final TypeElement masterElement;

        private final String metacodeSimpleName;
        private final String metacodeCanonicalName;
        private final Set<Class<? extends Annotation>> metacodeAnnotations;

        public MetacodeContextImpl(Elements elementUtils, TypeElement masterElement) {
            this.masterElement = masterElement;
            this.metacodeAnnotations = new HashSet<>();

            metacodeCanonicalName = MetacodeUtils.getMetacodeOf(elementUtils, masterElement.toString());
            int i = metacodeCanonicalName.lastIndexOf('.');
            metacodeSimpleName = i >= 0 ? metacodeCanonicalName.substring(i + 1) : metacodeCanonicalName;
        }


        @Override
        public TypeElement masterElement() {
            return masterElement;
        }

        @Override
        public Set<Class<? extends Annotation>> metacodeAnnotations() {
            return metacodeAnnotations;
        }
    }

    private static class Messager implements Logger {
        private javax.annotation.processing.Messager messager;
        private boolean debug = true;

        @Override
        public void debug(String msg) {
            if (debug) {
                messager.printMessage(Diagnostic.Kind.NOTE, msg);
            }
        }

        @Override
        public void warn(String msg) {
            messager.printMessage(Diagnostic.Kind.WARNING, msg);
        }

        @Override
        public void error(String msg) {
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
        }
    }

    private static class MasterTypePredicate implements Predicate<MetacodeContext> {
        private final TypeElement masterTypeElement;

        public MasterTypePredicate(TypeElement masterTypeElement) {
            this.masterTypeElement = masterTypeElement;
        }

        @Override
        public boolean apply(MetacodeContext input) {
            return input.masterElement().equals(masterTypeElement);
        }
    }

    private static class MetasitoryEnvironmentImpl implements MetasitoryEnvironment {
        private ProcessingEnvironment env;
        private Logger logger;

        public ProcessingEnvironment processingEnv() {
            return env;
        }

        public Logger logger() {
            return logger;
        }
    }
}
