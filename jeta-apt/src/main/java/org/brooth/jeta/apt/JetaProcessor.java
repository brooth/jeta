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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.brooth.jeta.IMetacode;
import org.brooth.jeta.apt.metasitory.MapMetasitoryWriter;
import org.brooth.jeta.apt.metasitory.MetasitoryEnvironment;
import org.brooth.jeta.apt.metasitory.MetasitoryWriter;
import org.brooth.jeta.apt.processors.ImplementationProcessor;
import org.brooth.jeta.apt.processors.LogProcessor;
import org.brooth.jeta.apt.processors.MetaEntityProcessor;
import org.brooth.jeta.apt.processors.MetaProcessor;
import org.brooth.jeta.apt.processors.MultitonProcessor;
import org.brooth.jeta.apt.processors.ObjectCollectorProcessor;
import org.brooth.jeta.apt.processors.ObservableProcessor;
import org.brooth.jeta.apt.processors.ObserverProcessor;
import org.brooth.jeta.apt.processors.ProxyProcessor;
import org.brooth.jeta.apt.processors.PublishProcessor;
import org.brooth.jeta.apt.processors.SingletonProcessor;
import org.brooth.jeta.apt.processors.SubscribeProcessor;
import org.brooth.jeta.apt.processors.TypeCollectorProcessor;
import org.brooth.jeta.apt.processors.ValidateProcessor;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * jetaDebug=true                                  - debug output 
 * jetaMetasitory=com.example.MyMetasitory         - set metasitory 
 * jetaProcessor=com.example.apt.MyCustomProcessor - add custom processors (comma separated) 
 * jetaDisable=Meta.*,Log                          - disable processors
 * jetaUtdSrcDir=/path-to-source-dir               - check source code is up-to-date to skip metacode generationg
 * jetaTempDir=/path-to-temp-dir                   - directory where to same processing results
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
    private Properties utdProperties;
    private String sourcepath;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.env = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();

        logger = new Messager();
        logger.messager = processingEnv.getMessager();
        logger.debug = Boolean.parseBoolean(Optional.fromNullable(env.getOptions().get("jetaDebug")).or("false"));
        logger.debug("init");

        try {
            processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH,
                "org", "jeta.properties");
            
        } catch(Exception e){
            throw new RuntimeException(e);
        }

        if (processingEnv.getOptions().containsKey("jetaUtdSrcDir")) {
            sourcepath = processingEnv.getOptions().get("jetaUtdSrcDir");
            utdProperties = new Properties();
            File props = new File(getUtdPropertiesFilePath());
            if (props.exists())
                try {
                    utdProperties.load(new FileInputStream(props));

                } catch (IOException e) {
                    // really?
                }
        }

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

        String addProcessors = processingEnv.getOptions().get("jetaProcessors");
        if (addProcessors != null) {
            for (String addProcessor : addProcessors.split(",")) {
                try {
                    Class<?> processorClass = Class.forName(addProcessor.trim());
                    processors.add((Processor) processorClass.newInstance());

                } catch (Exception e) {
                    throw new RuntimeException("Failed to load processor " + addProcessor, e);
                }
            }
        }
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

            if (utdProperties != null) {
                try {
                    String filePath = getUtdPropertiesFilePath();
                    logger.debug("storing processing result to " + filePath);
                    OutputStream fos = new FileOutputStream(filePath);
                    utdProperties.store(fos, null);

                } catch (IOException e) {
                    logger.warn("failed to save processing data for up-to-date feature usage, error: " 
                        + e.getMessage());
                }
            }
        }

        if (blankRounds == 1) {
            logger.debug(String.format("built in %dms", System.currentTimeMillis() - ts));
        }

        blankRounds++;
        return true;
    }

    private String getUtdPropertiesFilePath() {
        String tmpDir = processingEnv.getOptions().containsKey("jetaTempDir")
                ? processingEnv.getOptions().get("jetaTempDir") : System.getProperty("java.io.tmpdir");
        if (!tmpDir.endsWith(File.separator))
            tmpDir += File.separator;

        String fileName = sourcepath.replaceAll("[^a-zA-Z_]", "_") + ".jetadata";
        return tmpDir + fileName;
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

        Iterator<MetacodeContextImpl> iter = metacodeContextList.iterator();
        while (iter.hasNext() ) {
            MetacodeContextImpl context  = iter.next();
            if (utdProperties != null && !utdProperties.isEmpty()) {
                try {
                    String path  = processingEnv.getFiler().getResource(
                            StandardLocation.SOURCE_OUTPUT,
                            context.metacodePackage,
                            context.metacodeSimpleName + ".java").toUri().getPath();

                    if (utdProperties.containsKey(path)) {
                        File sourceJavaFile = new File(getSourceJavaFile(context.masterElement));
                        if (utdProperties.get(path).equals(String.valueOf(sourceJavaFile.lastModified()))) {
                            logger.debug("    * " + context.metacodeCanonicalName + " up-to-date");
                            utdProperties.remove(path);
                            context.utd = true;
                            context.processorEnvironments.clear();
                            continue;
                        }
                    }

                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
            
            logger.debug("    + " + context.metacodeCanonicalName);
            ClassName masterClassName = ClassName.get(context.masterElement);
            TypeSpec.Builder builder = TypeSpec.classBuilder(context.metacodeSimpleName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addJavadoc("Generated by Jeta, http://brooth.jeta.org\n")
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(IMetacode.class), masterClassName));

            builder.addMethod(MethodSpec.methodBuilder("getMasterClass").addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), masterClassName))
                    .addStatement("return $T.class", masterClassName).build());

            context.builder = builder;
        }
    }

    private String getSourceJavaFile(Element element) {
        return sourcepath + File.separator
                + MetacodeUtils.sourceElementOf(element).toString().replace(".", File.separator) + ".java";
    }

    private boolean processMetacodes(RoundEnvironment roundEnv) {
        boolean reclaim = false;
        Iterator<MetacodeContextImpl> iterator = metacodeContextList.iterator();
        while (iterator.hasNext()) {
            MetacodeContextImpl context = iterator.next();
            Iterator<Map.Entry<Processor, ProcessorEnvironmentImpl>> processorEnvIterator = 
                context.processorEnvironments.entrySet().iterator();

            while (processorEnvIterator.hasNext()) {
                Map.Entry<Processor, ProcessorEnvironmentImpl> entry = processorEnvIterator.next();
                Processor processor = entry.getKey();
                ProcessorEnvironmentImpl processorEnvironment = entry.getValue();

                logger.debug("processing " + context.metacodeCanonicalName + " with "
                        + processor.getClass().getSimpleName());

                processorEnvironment.roundEnvironment = roundEnv;
                processorEnvironment.round = round;
                if (!processor.process(processorEnvironment, context.builder))
                    processorEnvIterator.remove();

                if (processor.needReclaim())
                    reclaim = true;
            }

            if (context.processorEnvironments.isEmpty()) {
                // up-to-date?
                if(context.builder != null) {
                    String pkg = env.getElementUtils().getPackageOf(context.masterElement).getQualifiedName().toString();
                    JavaFile.Builder fileBuilder = JavaFile.builder(pkg, context.builder.build()).indent("\t");
                    if (env.getOptions().containsKey("jetaFileComment"))
                        fileBuilder.addFileComment(env.getOptions().get("jetaFileComment"));

                    Writer out = null;
                    try {
                        JavaFileObject sourceFile = processingEnv.getFiler()
                            .createSourceFile(context.metacodeCanonicalName);
                        JavaFile javaFile = fileBuilder.build();
                        out = sourceFile.openWriter();
                        javaFile.writeTo(out);
                        out.close();

                        if (utdProperties != null) {
                            File sourceJavaFile = new File(getSourceJavaFile(context.masterElement));
                            utdProperties.put(sourceFile.toUri().getPath(), String.valueOf(sourceJavaFile.lastModified()));
                        }

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

                    logger.debug("generating " + context.metacodeCanonicalName + " complete");
                }

                metasitoryWriter.write(context);
                iterator.remove();
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
        @Nullable
        private TypeSpec.Builder builder;
        private Map<Processor, ProcessorEnvironmentImpl> processorEnvironments = new HashMap<>();

        private final TypeElement masterElement;

        private final String metacodePackage;
        private final String metacodeSimpleName;
        private final String metacodeCanonicalName;
        private final Set<Class<? extends Annotation>> metacodeAnnotations;

        private boolean utd = false;

        public MetacodeContextImpl(Elements elementUtils, TypeElement masterElement) {
            this.masterElement = masterElement;
            this.metacodeAnnotations = new HashSet<>();

            metacodeCanonicalName = MetacodeUtils.getMetacodeOf(elementUtils, masterElement.toString());
            int i = metacodeCanonicalName.lastIndexOf('.');
            metacodePackage = i > 0 ? metacodeCanonicalName.substring(0, i) : "";
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

        @Override
        public boolean isUpToDate() {
            return utd;
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
