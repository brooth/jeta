package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.apt.metasitory.HasMapMetasitoryWriter;
import com.github.brooth.metacode.apt.metasitory.MetasitoryWriter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.*;

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
    private Elements elementUtils;

    private List<Processor> processors = new ArrayList<>();
    private List<MetacodeContextImpl> metacodeContextList = new ArrayList<>(500);

    private MetasitoryWriter metasitoryWriter;

    private int round = 0;
    private int blankRounds = 0;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "init");

        this.env = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();

        processors.add(new ObservableProcessor());
        processors.add(new ObserverProcessor());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "processing. round: " + (++round));

        if (round == 1) {
            metacodeContextList.clear();
            assembleMetacodeContextList(roundEnv);
        }

        if (!metacodeContextList.isEmpty()) {
            if (round == 1) {
        		metasitoryWriter = new HasMapMetasitoryWriter();
                metasitoryWriter.open(env);
                generateMetaSkeletons();
            }

            return !processAutoCodes(roundEnv);
        }

        if (round > 1 && blankRounds == 0) {
            metasitoryWriter.close();
        }

        blankRounds++;
        return true;
    }

    private void generateMetaSkeletons() {
        messager.printMessage(Diagnostic.Kind.NOTE, "generating meta skeletons");

		for (MetacodeContextImpl context : metacodeContextList) {
        	messager.printMessage(Diagnostic.Kind.NOTE, "meta skeleton for: " + context.metacodeCanonicalName);

			ClassName masterClassName = ClassName.bestGuess(context.masterCanonicalName);

            TypeSpec.Builder builder = TypeSpec.classBuilder(context.metacodeSimpleName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)    
                    .addSuperinterface(ParameterizedTypeName.get(
						ClassName.get(MasterMetacode.class), masterClassName));

			builder.addMethod(MethodSpec.methodBuilder("getMasterClass")
			    .addModifiers(Modifier.PUBLIC)
			    .returns(ParameterizedTypeName.get(
					ClassName.get(Class.class), masterClassName))
			    .addStatement("return $T.class", masterClassName)
			    .build());

            context.builder = builder;
        }
    }

    private boolean processAutoCodes(RoundEnvironment roundEnv) {
		boolean reclaim = false;
        Iterator<MetacodeContextImpl> iterator = metacodeContextList.iterator();
        while (iterator.hasNext()) {
            MetacodeContextImpl context = iterator.next();
            Iterator<Map.Entry<Processor, ProcessorContext>> processorContextIterator
                    = context.processorContextMap.entrySet().iterator();
            while (processorContextIterator.hasNext()) {
			    Map.Entry<Processor, ProcessorContext> entry = processorContextIterator.next();
                Processor processor = entry.getKey();
                ProcessorContext processorContext = entry.getValue();

                messager.printMessage(Diagnostic.Kind.NOTE, "processing " +
                        context.metacodeCanonicalName + " with " + processor.getClass().getSimpleName());

                if (!processor.process(roundEnv, processorContext, context.builder, round))
                    processorContextIterator.remove();

				if(processor.needReclaim())
					reclaim = true;
            }

            if (context.processorContextMap.isEmpty()) {
                JavaFile javaFile = JavaFile.builder(context.getMasterPackage(), context.builder.build()).build();

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
                messager.printMessage(Diagnostic.Kind.NOTE, "generating " + context.metacodeCanonicalName + " complete");
            }
        }
		return reclaim;
    }

    private void assembleMetacodeContextList(RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "assemble metacode context list");

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
        		messager.printMessage(Diagnostic.Kind.NOTE, "collecting elements with @" + annotation.getSimpleName());
                
				Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
                if (elements.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "no elements found with @" + annotation.getSimpleName());
                    continue;
                }

                // go through annotated elements
                for (Element element : elements) {
        			messager.printMessage(Diagnostic.Kind.NOTE, "found element: " + element.toString());
                    
					// go through element's masters
                    for (TypeElement masterTypeElement : processor.applicableMastersOfElement(env, element)) {
        				messager.printMessage(Diagnostic.Kind.NOTE, "applicable master: " + masterTypeElement.toString());
                        
                        MetacodeContextImpl context = Iterables.find(metacodeContextList,
                                new MasterTypePredicate(masterTypeElement), null);
						if(context == null) {
                        	context = new MetacodeContextImpl(elementUtils, masterTypeElement);
							metacodeContextList.add(context);

        					messager.printMessage(Diagnostic.Kind.NOTE, "   masterPackage         - " + context.masterPackage);
        					messager.printMessage(Diagnostic.Kind.NOTE, "   masterSimpleName      - " + context.masterSimpleName);
        					messager.printMessage(Diagnostic.Kind.NOTE, "   masterCanonicalName   - " + context.masterCanonicalName);
							messager.printMessage(Diagnostic.Kind.NOTE, "   masterFlatName        - " + context.masterFlatName);
        					messager.printMessage(Diagnostic.Kind.NOTE, "   sourceCanonicalName   - " + context.sourceCanonicalName);
        					messager.printMessage(Diagnostic.Kind.NOTE, "   metacodeSimpleName    - " + context.metacodeSimpleName);
        					messager.printMessage(Diagnostic.Kind.NOTE, "   metacodeCanonicalName - " + context.metacodeCanonicalName);
						}

                        ProcessorContext processorContext = context.processorContextMap.get(processor);
                        if (processorContext == null) {
                            processorContext = new ProcessorContext();
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
        private TypeSpec.Builder builder;
        private Map<Processor, ProcessorContext> processorContextMap = new HashMap<>();

        private String masterPackage;
        private String masterCanonicalName;
        private String masterSimpleName;
        private String metacodeSimpleName;
        private String metacodeCanonicalName;
		private String masterFlatName;
        private String sourceCanonicalName;

        public MetacodeContextImpl(Elements elementUtils, TypeElement masterTypeElement) {
			masterPackage = elementUtils.getPackageOf(masterTypeElement).getQualifiedName().toString(); 
            masterCanonicalName = masterTypeElement.toString();
            masterSimpleName = masterTypeElement.getSimpleName().toString();
			sourceCanonicalName = MetacodeUtils.getSourceTypeElement(masterTypeElement).toString();
			masterFlatName = masterPackage + "." + masterCanonicalName.replace(masterPackage + ".", "").replaceAll("\\.", "\\$");
			metacodeCanonicalName = MetacodeUtils.getMetacodeOf(elementUtils, masterCanonicalName);
			int i = metacodeCanonicalName.lastIndexOf('.');     
			metacodeSimpleName = i >= 0 ? metacodeCanonicalName.substring(i + 1) : metacodeCanonicalName;
        }

        @Override
        public String getMasterPackage() {
            return masterPackage;
        }

        @Override
        public String getMasterCanonicalName() {
            return masterCanonicalName;
        }

        @Override
        public String getMasterSimpleName() {
            return masterSimpleName;
        }

        @Override
        public String getMasterFlatName() {
            return masterFlatName;
        }

        @Override
        public String getSourceCanonicalName() {
            return sourceCanonicalName;
        }

        @Override
        public Set<Class<? extends Annotation>> metacodeAnnotations() {
            return new HashSet<>();
        }
    }

    private static class MasterTypePredicate implements Predicate<MetacodeContext> {
        private final TypeElement masterTypeElement; 

        public MasterTypePredicate(TypeElement masterTypeElement) {
            this.masterTypeElement = masterTypeElement;
        }

        @Override
        public boolean apply(MetacodeContext input) {
            return input.getMasterCanonicalName().equals(masterTypeElement.toString());
        }
    }
}
