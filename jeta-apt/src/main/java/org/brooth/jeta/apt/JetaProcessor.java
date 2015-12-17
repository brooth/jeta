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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.*;
import org.brooth.jeta.IMetacode;
import org.brooth.jeta.apt.metasitory.MapMetasitoryWriter;
import org.brooth.jeta.apt.metasitory.MetasitoryEnvironment;
import org.brooth.jeta.apt.metasitory.MetasitoryWriter;
import org.brooth.jeta.apt.processors.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JetaProcessor extends AbstractProcessor {

    public static final String METACODE_CLASS_POSTFIX = "_Metacode";

    private Messager logger;
    private Elements elementUtils;

    private List<Processor> processors = new ArrayList<>();
    private List<MetacodeContextImpl> metacodeContextList = new ArrayList<>(500);

    private MetasitoryWriter metasitoryWriter;

    private int round = 0;
    private int blankRounds = 0;
    private long ts;

    private final Properties properties = new Properties();
    private Properties utdProperties = null;

    private String relateToPath = null;
    private String sourcePath = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();

        logger = new Messager();
        logger.messager = processingEnv.getMessager();
        logger.debug = false;
        logger.debug("init");

        loadProperties(processingEnv);
        addProcessors();
    }

    private void loadProperties(ProcessingEnvironment processingEnv) {
        Path propertiesPath = null;
        String propertiesName = "jeta.properties";
        if (processingEnv.getOptions().containsKey("jetaProperties")) {
            String filePath = processingEnv.getOptions().get("jetaProperties");
            try {
                propertiesPath = Paths.get(filePath);
                if (!propertiesPath.isAbsolute())
                    propertiesPath = propertiesPath.toAbsolutePath().normalize();

                propertiesName = propertiesPath.getFileName().toString();

            } catch (Exception e) {
                throw new IllegalArgumentException("failed to load properties from " + filePath);
            }

        } else {
            try {
                FileObject propertiesFileObject =
                        processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", propertiesName);
                propertiesPath = Paths.get(propertiesFileObject.toUri());

            } catch (Exception e) {
                logger.warn("jeta.properties not found, used default settings");
            }
        }

        if (propertiesPath != null) {
            try {
                InputStream is = Files.newInputStream(propertiesPath);
                properties.load(is);
                is.close();

            } catch (IOException e) {
                throw new RuntimeException("failed to load properties", e);
            }
        }

        if (propertiesPath == null) {
            relateToPath = Paths.get(".").toAbsolutePath().toString();
            if (!relateToPath.endsWith(File.separator))
                relateToPath += File.separator;

        } else {
            relateToPath = propertiesPath.toString().replace(propertiesName, "");
        }

        if (properties.containsKey("sourcepath")) {
            sourcePath = properties.getProperty("sourcepath");
            Path path = Paths.get(sourcePath);
            if (!path.isAbsolute()) {
                if (propertiesPath != null)
                    path = Paths.get(propertiesPath.toString().replace(propertiesName, sourcePath));

                sourcePath = path.toAbsolutePath().normalize().toString();
            }

            if (!sourcePath.endsWith(File.separator))
                sourcePath += File.separator;

        } else if (sourcePath == null) {
            sourcePath = relateToPath;
        }

        logger.debug = "true".equals(properties.getProperty("debug"));

        if ("true".equals(properties.getProperty("utd.enable"))) {
            utdProperties = new Properties();
            File props = new File(getUtdPropertiesFilePath());
            if (props.exists())
                try {
                    utdProperties.load(new FileInputStream(props));

                } catch (IOException e) {
                    logger.warn("failed to load: " + props.getPath() + ", error: " + e.getMessage());
                    utdProperties = null;
                }
        }
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

        String addProcessors = properties.getProperty("processors.add");
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
                createMetasitoryWriter();
                generateMetaTypeBuilders();
            }

            return !processMetacodes(roundEnv);
        }

        if (round > 1 && blankRounds == 0) {
            metasitoryWriter.close();

            if (utdProperties != null) {
                saveUtdProperties();
            }
        }

        if (blankRounds == 1) {
            logger.debug(String.format("built in %dms", System.currentTimeMillis() - ts));
        }

        blankRounds++;
        return true;
    }

    protected void removeDisabledProcessors() {
        String[] disabled = properties.getProperty("processors.disable", "").split(",");
        Iterator<Processor> iter = processors.iterator();
        while (iter.hasNext()) {
            Processor processor = iter.next();
            boolean enabled = processor.isEnabled(processingEnv);

            if (enabled && disabled.length > 0) {
                Set<Class<? extends Annotation>> annotations = processor.collectElementsAnnotatedWith();
                for (String disable : disabled)
                    for (Class<? extends Annotation> annotation : annotations)
                        if (annotation.getSimpleName().matches(disable.trim())) {
                            enabled = false;
                            break;
                        }
            }

            if (!enabled) {
                logger.debug("processor " + processor.getClass().getCanonicalName() + " disabled");
                iter.remove();
            }
        }
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
                    for (TypeElement masterTypeElement : processor.applicableMastersOfElement(processingEnv, element)) {
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
                            processorEnvironment.processingEnvironment = processingEnv;
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

    private MetasitoryEnvironmentImpl createMetasitoryWriter() {
        if (!properties.getProperty("metasitory.writer", "").isEmpty()) {
            try {
                String className = properties.getProperty("metasitory.writer");
                metasitoryWriter = (MetasitoryWriter) Class.forName(className).newInstance();

            } catch (Exception e) {
                throw new IllegalArgumentException("failed to create metasitory writer", e);
            }

        } else {
            metasitoryWriter = new MapMetasitoryWriter();
        }

        MetasitoryEnvironmentImpl impl = new MetasitoryEnvironmentImpl();
        impl.env = processingEnv;
        impl.logger = logger;
        impl.properties = properties;

        metasitoryWriter.open(impl);
        return impl;
    }

    private void generateMetaTypeBuilders() {
        logger.debug("generating meta type builders");

        Properties utdPropertiesCopy = null;
        if (utdProperties != null)
            utdPropertiesCopy = (Properties) utdProperties.clone();

        for (MetacodeContextImpl context : metacodeContextList) {
            if (utdPropertiesCopy != null) {
                if (utdPropertiesCopy.containsKey(context.metacodeCanonicalName)) {
                    context.utd = true;
                    for (Processor processor : context.processorEnvironments.keySet())
                        if (!(processor instanceof UtdProcessor) ||
                                ((UtdProcessor) processor).ignoreMasterUpToDate(
                                        context.processorEnvironments.get(processor))) {
                            context.utd = false;
                            break;
                        }

                    if (context.utd) {
                        File sourceJavaFile = new File(getSourceJavaFile(context.masterElement));
                        if (!sourceJavaFile.exists())
                            throw new IllegalStateException("can't find source file: " + sourceJavaFile.getPath() +
                                    ", set 'sourcepath' property to the source dir path");
                        context.utd = utdPropertiesCopy.get(context.metacodeCanonicalName).equals(
                                String.valueOf(sourceJavaFile.lastModified()));
                    }

                    utdPropertiesCopy.remove(context.metacodeCanonicalName);

                    if (context.utd) {
                        try {
                            JavaFileObject sourceJavaFile = processingEnv.getFiler().createSourceFile(context.metacodeCanonicalName);
                            Path source = Paths.get(sourceJavaFile.toUri());
                            Files.delete(source);
                            sourceJavaFile.openWriter().close();
                            Files.delete(source);
                            Path target = Paths.get(getUtdDirPath() + context.metacodeCanonicalName + ".utd");
                            Files.createLink(source, target);

                            logger.debug("    * " + context.metacodeCanonicalName + " up-to-date");
                            context.processorEnvironments.clear();
                            continue;

                        } catch (IOException e) {
                            throw new RuntimeException("failed to create link to .utd file", e);
                        }
                    }
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

        if (utdPropertiesCopy != null && !utdPropertiesCopy.isEmpty()) {
            if (properties.getProperty("utd.cleanup", "true").equals("true")) {
                for (Object key : utdPropertiesCopy.keySet()) {
                    String metacodeCanonicalName = (String) key;
                    int dot = metacodeCanonicalName.lastIndexOf('.');

                    try {
                        FileObject file = processingEnv.getFiler().getResource(
                                StandardLocation.SOURCE_OUTPUT,
                                (dot > 0 ? metacodeCanonicalName.substring(0, dot) : ""),
                                (dot > 0 ? metacodeCanonicalName.substring(dot + 1) : metacodeCanonicalName) + ".java");

                        if (new File(file.toUri()).delete()) {
                            logger.debug("    - " + metacodeCanonicalName + " removed");
                            utdProperties.remove(metacodeCanonicalName);

                        } else {
                            logger.warn("failed to cleanup metacode file: " + metacodeCanonicalName);
                        }

                    } catch (IOException e) {
                        logger.warn("failed to cleanup metacode file: " + metacodeCanonicalName + " error: " + e.getMessage());
                    }
                }
            }
            utdPropertiesCopy.clear();
        }
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
                if (!context.utd) {
                    String pkg = processingEnv.getElementUtils().getPackageOf(context.masterElement).getQualifiedName().toString();
                    JavaFile.Builder fileBuilder = JavaFile.builder(pkg, context.builder.build()).indent("\t");
                    if (properties.containsKey("file.comment"))
                        fileBuilder.addFileComment(properties.getProperty("file.comment"));

                    Writer out = null;
                    try {
                        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(context.metacodeCanonicalName);
                        JavaFile javaFile = fileBuilder.build();
                        out = sourceFile.openWriter();
                        javaFile.writeTo(out);
                        out.close();

                        if (utdProperties != null) {
                            File sourceJavaFile = new File(getSourceJavaFile(context.masterElement));
                            utdProperties.put(context.metacodeCanonicalName, String.valueOf(sourceJavaFile.lastModified()));

                            // move actual source file to utd dir and create a hard link to it. this trick is
                            // necessary to be able to simulate generating source file if its master is up to date.
                            // instead of real generating, new link will be created to the .utd file
                            Path source = Paths.get(sourceFile.toUri());
                            Path target = Paths.get(getUtdDirPath() + context.metacodeCanonicalName + ".utd");
                            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                            Files.createLink(source, target);
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

    private String getUtdPropertiesFilePath() {
        return getUtdDirPath() + "utd.properties";
    }

    private String getUtdDirPath() {
        String result = properties.getProperty("utd.dir");
        if (result != null) {
            Path path = Paths.get(result);
            if (!path.isAbsolute())
                path = Paths.get(relateToPath + result).toAbsolutePath();

            if (Files.notExists(path) && !path.toFile().mkdirs())
                throw new RuntimeException("not valid utd.dir property", new IOException("failed to create utd.dir structure"));

            result = path.normalize().toString();

        } else {
            result = System.getProperty("java.io.tmpdir");
            if (!result.endsWith(File.separator))
                result += File.separator;

            result = result + sourcePath.replaceAll("[^a-zA-Z_]", "_");
            Path path = Paths.get(result);
            if (Files.notExists(path) && !path.toFile().mkdirs())
                throw new RuntimeException("not valid utd.dir property", new IOException("failed to create utd.dir structure"));
        }

        if (!result.endsWith(File.separator))
            result += File.separator;

        return result;
    }

    private void saveUtdProperties() {
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

    private String getSourceJavaFile(Element element) {
        return sourcePath + MetacodeUtils.sourceElementOf(element).toString().replace(".", File.separator) + ".java";
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

    private static class MetacodeContextImpl implements UtdMetacodeContext {
        private TypeSpec.Builder builder;
        private Map<Processor, ProcessorEnvironmentImpl> processorEnvironments = new HashMap<>();

        private final TypeElement masterElement;

        private final String metacodeSimpleName;
        private final String metacodeCanonicalName;
        private final Set<Class<? extends Annotation>> metacodeAnnotations;

        private boolean utd = false;

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
        private Properties properties;

        public ProcessingEnvironment processingEnv() {
            return env;
        }

        public Logger logger() {
            return logger;
        }

        @Override
        public Properties jetaProperties() {
            return properties;
        }
    }
}
