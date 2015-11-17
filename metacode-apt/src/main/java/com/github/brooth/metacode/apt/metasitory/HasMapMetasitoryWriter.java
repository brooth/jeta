package com.github.brooth.metacode.apt.metasitory;

import com.github.brooth.metacode.apt.MetacodeContext;
import com.github.brooth.metacode.apt.MetacodeUtils;
import com.github.brooth.metacode.metasitory.HashMapMetasitoryContainer;
import com.github.brooth.metacode.observer.Observers;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.*;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.inject.Provider;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * -AmcMetasitoryPackage=com.example             - metasitory package
 * -AmcSuperMetasitoryPackage=com.example        - super metasitory package. ???
 */
public class HasMapMetasitoryWriter implements MetasitoryWriter {

    protected ProcessingEnvironment env;
    protected Messager messager;

    protected TypeSpec.Builder typeBuilder;
    protected MethodSpec.Builder methodBuilder;

    @Override
    public void open(ProcessingEnvironment env) {
        this.env = env;
        messager = env.getMessager();

        typeBuilder = TypeSpec.classBuilder("MetasitoryContainer")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeName.get(HashMapMetasitoryContainer.class));

        ClassName mapClassName = ClassName.get(Map.class);
        TypeName classTypeName = TypeName.get(Class.class);
        TypeName contextTypeName = TypeName.get(HashMapMetasitoryContainer.Context.class);

        methodBuilder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("$T<$T, $T> result = new $T<>()", mapClassName, classTypeName, contextTypeName,
                        TypeName.get(LinkedHashMap.class))
                .returns(ParameterizedTypeName.get(mapClassName, classTypeName, contextTypeName));
    }

    @Override
    public void write(MetacodeContext context) {
        String master = context.getMasterCanonicalName();
        String metacode = MetacodeUtils.getMetacodeOf(env.getElementUtils(), master);
        String annotations = Joiner.on(",").join(
                Iterables.transform(context.metacodeAnnotations(), new Function<Class<? extends Annotation>, String>() {
                    @Override
                    public String apply(Class<? extends Annotation> input) {
                        return "\n\t\t\t" + input.getCanonicalName() + ".class";
                    }
                }));
        methodBuilder.addCode("result.put($L.class,\n" +
                        "\tnew com.github.brooth.metacode.metasitory.HashMapMetasitoryContainer.Context(\n" +
                        "\t\t$L.class,\n" +
                        "\t\tnew javax.inject.Provider<$L>() {\n" +
                        "\t\t\tpublic $L get() {\n" +
                        "\t\t\t\treturn new $L();\n" +
                        "\t\t}},\n" +
                        "\t\tnew Class[] {$L\n\t\t}));\n",
                master, master, metacode, metacode, metacode, annotations);
    }

    @Override
    public void close() {
        messager.printMessage(Diagnostic.Kind.NOTE, "writing metasitory");

        String metasitoryPackage = env.getOptions().get("mcMetasitoryPackage");
        if (metasitoryPackage == null) {
            messager.printMessage(Diagnostic.Kind.WARNING, "mcMetasitoryPackage not present. used root package");
            metasitoryPackage = "";
        }

        methodBuilder.addStatement("return result");
        typeBuilder.addMethod(methodBuilder.build());

        JavaFile javaFile = JavaFile.builder(metasitoryPackage, typeBuilder.build()).build();
        Writer out = null;
        try {
            JavaFileObject sourceFile = env.getFiler().createSourceFile("MetasitoryContainer");
            out = sourceFile.openWriter();
            javaFile.writeTo(out);
            out.close();

        } catch (IOException e) {
            throw new RuntimeException("failed to write metasitory file", e);

        } finally {
            if (out != null)
                try {
                    out.close();

                } catch (IOException e) {
                    // why god, why?
                }
        }
    }
}
