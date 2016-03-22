/*
 * Copyright 2016 Oleg Khalidov
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
 *
 */

package org.brooth.jeta.apt;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.hash.Hashing;
import com.squareup.javapoet.*;
import org.brooth.jeta.IMetacode;
import org.brooth.jeta.apt.metasitory.MapMetasitoryWriter;
import org.brooth.jeta.apt.metasitory.MetasitoryWriter;
import org.brooth.jeta.apt.processors.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * todo: compile on java 1.5
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@SupportedAnnotationTypes("*")
public class JetaProcessor extends AbstractProcessor {

    public static final String METACODE_CLASS_POSTFIX = "_Metacode";

    private Messager logger;

    private List<Processor> processors = new ArrayList<Processor>();
    private List<MetacodeContextImpl> metacodeContextList = new ArrayList<MetacodeContextImpl>(500);

    private MetasitoryWriter metasitoryWriter;

    private int round = 0;
    private int blankRounds = 0;
    private long ts = 0;

    private final Properties properties = new Properties();
    private Properties utdProperties = null;

    private String relateToPath = null;
    private String sourcePath = null;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

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
                throw new IllegalArgumentException("failed to load properties from file " + filePath);
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
                throw new ProcessingException("failed to load properties from file " + propertiesPath, e);
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
        addProcessor(new ObservableProcessor());
        addProcessor(new ObserverProcessor());
        addProcessor(new ProxyProcessor());
        addProcessor(new SubscribeProcessor());
        addProcessor(new ValidateProcessor());
        addProcessor(new TypeCollectorProcessor());
        addProcessor(new ObjectCollectorProcessor());
        addProcessor(new LogProcessor());
        addProcessor(new SingletonProcessor());
        addProcessor(new MultitonProcessor());
        addProcessor(new ImplementationProcessor());
        addProcessor(new MetaModuleProcessor());
        addProcessor(new MetaScopeProcessor());
        addProcessor(new MetaInjectProcessor());
        addProcessor(new MetaEntityProcessor());

        String addProcessors = properties.getProperty("processors.add");
        if (addProcessors != null) {
            for (String addProcessor : addProcessors.split(",")) {
                try {
                    Class<?> processorClass = Class.forName(addProcessor.trim());
                    addProcessor((Processor) processorClass.newInstance());

                } catch (Exception e) {
                    throw new ProcessingException("Failed to load processor " + addProcessor, e);
                }
            }
        }

        for (Processor processor : processors) {
            ProcessingContextImpl processorEnv = new ProcessingContextImpl();
            processorEnv.processingEnv = processingEnv;
            processorEnv.processorProperties = (Properties) properties.clone();
            processorEnv.logger = logger;
            processor.init(processorEnv);
        }
    }

    protected void addProcessor(Processor processor) {
        processors.add(processor);
    }

    protected boolean removeProcessor(Processor processor) {
        return processors.remove(processor);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.debug("processing. round: " + (++round));

        if (round == 1) {
            if (properties.getProperty("debug.built_time", "true").equals("true"))
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

        if (blankRounds == 1 && ts > 0) {
            logger.note(String.format("Metacode built in %dms", System.currentTimeMillis() - ts));
        }

        blankRounds++;
        return true;
    }

    protected void removeDisabledProcessors() {
        String[] disabled = properties.getProperty("processors.disable", "").split(",");
        Iterator<Processor> iter = processors.iterator();
        while (iter.hasNext()) {
            Processor processor = iter.next();
            boolean enabled = processor.isEnabled();

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
                    // yeah, happens...
                    Annotation _an = null;
                    Exception _ex = null;
                    try {
                        _an = element.getAnnotation(annotation);
                    } catch (Exception e) {
                        _ex = e;
                    }

                    if (_an == null) {
                        String elementStr = element.toString();
                        if (!element.getKind().isClass() && !element.getKind().isInterface())
                            elementStr = element.getEnclosingElement().toString() + "." + elementStr;

                        String msg = "Failed to process \"" + elementStr + "\". " +
                                " Check it for potential compilation issues.";
                        if(_ex != null)
                            throw new ProcessingException(msg, _ex);
                        else
                            throw new ProcessingException(msg);
                    }

                    logger.debug("found element: " + element.toString());

                    // go through element's masters
                    for (TypeElement masterTypeElement : processor.applicableMastersOfElement(element)) {
                        logger.debug("applicable master: " + masterTypeElement.toString());

                        MetacodeContextImpl context = Iterables.find(metacodeContextList,
                                new MasterTypePredicate(masterTypeElement), null);
                        if (context == null) {
                            context = new MetacodeContextImpl(masterTypeElement);
                            metacodeContextList.add(context);
                            logger.debug("         metacode: " + context.metacodeCanonicalName);
                        }
                        context.metacodeAnnotations().add(annotation);

                        if (!context.processors.containsKey(processor))
                            context.processors.put(processor, element);
                        else
                            context.processors.get(processor).add(element);
                    }
                }
            }
        }
    }

    private void createMetasitoryWriter() {
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

        ProcessingContextImpl impl = new ProcessingContextImpl();
        impl.processingEnv = processingEnv;
        impl.logger = logger;
        impl.processorProperties = (Properties) properties.clone();
        metasitoryWriter.open(impl);
    }

    private void generateMetaTypeBuilders() {
        logger.debug("generating meta type builders");

        Properties utdPropertiesCopy = null;
        if (utdProperties != null)
            utdPropertiesCopy = (Properties) utdProperties.clone();

        boolean debug = properties.getProperty("debug.utd_states", "true").equals("true");

        Iterator<MetacodeContextImpl> iter = metacodeContextList.iterator();
        while (iter.hasNext()) {
            MetacodeContextImpl context = iter.next();
            if (utdPropertiesCopy != null) {
                if (utdPropertiesCopy.containsKey(context.metacodeCanonicalName)) {
                    long modifiedTs = Long.parseLong(utdPropertiesCopy.getProperty(context.metacodeCanonicalName));
                    if (modifiedTs > 0) {
                        // check metacode exists
                        if (Files.exists(Paths.get(getMetacodeFileObject(context.metacodeCanonicalName).toUri()))) {
                            context.utd = true;
                            for (Processor processor : context.processors.keySet()) {
                                Path path = Paths.get(getSourceJavaFile(context.masterElement));
                                if (Files.exists(path)) {
                                    try {
                                        if (processor.ignoreUpToDate() ||
                                                Files.getLastModifiedTime(path).toMillis() != modifiedTs) {
                                            context.utd = false;
                                            break;
                                        }
                                    } catch (IOException e) {
                                        throw new ProcessingException("failed to read last modify date of " + path.toString(), e);
                                    }
                                } else {
                                    logger.debug("Can't check utd state. No source file of " + path.toUri().getPath());
                                    context.utd = false;
                                }


                            }

                        } else {
                            logger.debug(context.metacodeCanonicalName + " source file not exists. utd checking skipped");
                        }
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

                            if (debug)
                                logger.note("    * " + context.metacodeCanonicalName + " up-to-date");

                            metasitoryWriter.write(context);
                            iter.remove();
                            continue;

                        } catch (IOException e) {
                            throw new ProcessingException("failed to create link to .utd file", e);
                        }
                    }
                }
            }

            if (debug)
                logger.note("    + " + context.metacodeCanonicalName);

            ClassName masterClassName = ClassName.get(context.masterElement);
            TypeSpec.Builder builder = TypeSpec.classBuilder(context.metacodeSimpleName)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("Generated by Jeta, http://jeta.brooth.org\n")
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(IMetacode.class), masterClassName));

            builder.addMethod(MethodSpec.methodBuilder("getMasterClass").addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), masterClassName))
                    .addStatement("return $T.class", masterClassName).build());

            context.builder = builder;
        }

        if (utdPropertiesCopy != null && !utdPropertiesCopy.isEmpty() &&
                properties.getProperty("utd.cleanup", "true").equals("true")) {
            for (Object key : utdPropertiesCopy.keySet()) {
                String metacodeCanonicalName = (String) key;
                FileObject file = getMetacodeFileObject(metacodeCanonicalName);
                if (new File(file.toUri()).delete()) {
                    if (debug)
                        logger.note("    - " + metacodeCanonicalName + " removed");
                    utdProperties.remove(metacodeCanonicalName);

                } else {
                    logger.warn("failed to cleanup metacode file: " + metacodeCanonicalName);
                }
            }
        }
    }

    private FileObject getMetacodeFileObject(String canonicalName) {
        int dot = canonicalName.lastIndexOf('.');
        try {
            return processingEnv.getFiler().getResource(
                    StandardLocation.SOURCE_OUTPUT,
                    (dot > 0 ? canonicalName.substring(0, dot) : ""),
                    (dot > 0 ? canonicalName.substring(dot + 1) : canonicalName) + ".java");

        } catch (IOException e) {
            throw new ProcessingException(e);
        }
    }

    private boolean processMetacodes(RoundEnvironment roundEnv) {
        boolean reclaim = false;
        Iterator<MetacodeContextImpl> iterator = metacodeContextList.iterator();
        while (iterator.hasNext()) {
            MetacodeContextImpl context = iterator.next();
            Map<Processor, Collection<Element>> processorsMap = context.processors.asMap();
            Iterator<Processor> processorsIterator = processorsMap.keySet().iterator();
            while (processorsIterator.hasNext()) {
                Processor processor = processorsIterator.next();

                logger.debug("processing " + context.metacodeCanonicalName + " with "
                        + processor.getClass().getSimpleName());

                RoundContextImpl roundContext = new RoundContextImpl();
                roundContext.roundEnv = roundEnv;
                roundContext.round = round;
                roundContext.metacodeContext = context;
                roundContext.elements = processorsMap.get(processor);

                if (!processor.process(context.builder, roundContext))
                    processorsIterator.remove();

                if (processor.needReclaim())
                    reclaim = true;
            }

            if (context.processors.isEmpty()) {
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
                        if (Files.exists(target))
                            Files.delete(target);
                        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                        Files.createLink(source, target);
                    }

                } catch (IOException e) {
                    throw new ProcessingException("failed to write metacode file", e);

                } finally {
                    if (out != null)
                        try {
                            out.close();

                        } catch (IOException e) {
                            // os corrupted successfully
                        }
                }

                logger.debug("generating " + context.metacodeCanonicalName + " complete");

                metasitoryWriter.write(context);
                iterator.remove();
            }
        }
        return reclaim;
    }

    private String getUtdPropertiesFilePath() {
        return getUtdDirPath() + "utd.properties";
    }

    private String $utdDirPath;

    private String getUtdDirPath() {
        if ($utdDirPath != null)
            return $utdDirPath;

        $utdDirPath = properties.getProperty("utd.dir");
        if ($utdDirPath != null) {
            Path path = Paths.get($utdDirPath);
            if (!path.isAbsolute())
                path = Paths.get(relateToPath + $utdDirPath).toAbsolutePath();

            $utdDirPath = path.normalize().toString();

        } else {
            $utdDirPath = System.getProperty("java.io.tmpdir");
            if (!$utdDirPath.endsWith(File.separator))
                $utdDirPath += File.separator;

            $utdDirPath += "jeta-utd-files";
        }

        if (!$utdDirPath.endsWith(File.separator))
            $utdDirPath += File.separator;

        try {
            $utdDirPath += Hashing.adler32().newHasher().putString(processingEnv.getFiler().
                            getResource(StandardLocation.SOURCE_OUTPUT, "a", "b").toUri().getPath(),
                    Charset.defaultCharset()).hash().toString() + File.separator;

        } catch (IOException e) {
            throw new ProcessingException("failed to get upt dir hash", e);
        }

        Path structurePath = Paths.get($utdDirPath);
        if (Files.notExists(structurePath) && !structurePath.toFile().mkdirs())
            throw new ProcessingException("not valid utd.dir property", new IOException("failed to create utd.dir structure"));

        return $utdDirPath;
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

    private static class RoundContextImpl implements RoundContext {
        private MetacodeContext metacodeContext;
        private Collection<Element> elements;
        private RoundEnvironment roundEnv;
        private int round;

        public Collection<Element> elements() {
            return elements;
        }

        public MetacodeContext metacodeContext() {
            return metacodeContext;
        }

        public RoundEnvironment roundEnv() {
            return roundEnv;
        }

        public int round() {
            return round;
        }
    }

    private static class ProcessingContextImpl implements ProcessingContext {
        private ProcessingEnvironment processingEnv;
        private Properties processorProperties;
        private Messager logger;

        public ProcessingEnvironment processingEnv() {
            return processingEnv;
        }

        public Logger logger() {
            return logger;
        }

        public Properties processingProperties() {
            return processorProperties;
        }
    }

    private static class MetacodeContextImpl implements MetacodeContext {
        private Multimap<Processor, Element> processors;

        private TypeSpec.Builder builder;
        private final TypeElement masterElement;

        private final String metacodeSimpleName;
        private final String metacodeCanonicalName;
        private final Set<Class<? extends Annotation>> metacodeAnnotations;

        private boolean utd = false;

        public MetacodeContextImpl(TypeElement masterElement) {
            this.processors = HashMultimap.create();
            this.masterElement = masterElement;
            this.metacodeAnnotations = new HashSet<Class<? extends Annotation>>();

            metacodeCanonicalName = MetacodeUtils.toMetacodeName(masterElement.toString());
            int i = metacodeCanonicalName.lastIndexOf('.');
            metacodeSimpleName = i >= 0 ? metacodeCanonicalName.substring(i + 1) : metacodeCanonicalName;
        }

        public TypeElement masterElement() {
            return masterElement;
        }

        public Set<Class<? extends Annotation>> metacodeAnnotations() {
            return metacodeAnnotations;
        }

        public boolean isUpToDate() {
            return utd;
        }
    }

    private static class Messager implements Logger {
        private javax.annotation.processing.Messager messager;
        private boolean debug = true;

        public void debug(String msg) {
            if (debug) {
                messager.printMessage(Diagnostic.Kind.NOTE, msg);
            }
        }

        public void note(String msg) {
            messager.printMessage(Diagnostic.Kind.NOTE, msg);
        }

        public void warn(String msg) {
            messager.printMessage(Diagnostic.Kind.WARNING, msg);
        }

        public void error(String msg) {
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
        }
    }

    private static class MasterTypePredicate implements Predicate<MetacodeContext> {
        private final TypeElement masterTypeElement;

        public MasterTypePredicate(TypeElement masterTypeElement) {
            this.masterTypeElement = masterTypeElement;
        }

        public boolean apply(MetacodeContext input) {
            return input.masterElement().equals(masterTypeElement);
        }
    }
}
