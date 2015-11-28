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

package org.javameta.apt.metasitory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.*;
import org.javameta.apt.MetacodeContext;
import org.javameta.apt.MetacodeUtils;
import org.javameta.metasitory.MapMetasitoryContainer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.IdentityHashMap;
import java.util.Map;

import org.javameta.apt.Logger;

/**
 * jetaMapMetasitoryPackage=com.example             - metasitory package
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MapMetasitoryWriter implements MetasitoryWriter {

    protected ProcessingEnvironment env;
    protected Logger logger;

    protected TypeSpec.Builder typeBuilder;
    protected MethodSpec.Builder methodBuilder;

    @Override
    public void open(MetasitoryEnvironment env) {
        this.env = env.processingEnv();
        logger = env.logger();

        typeBuilder = TypeSpec.classBuilder("MetasitoryContainer")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeName.get(MapMetasitoryContainer.class));

        ClassName mapClassName = ClassName.get(Map.class);
        TypeName classTypeName = TypeName.get(Class.class);
        TypeName contextTypeName = TypeName.get(MapMetasitoryContainer.Context.class);

        methodBuilder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("$T<$T, $T> result = new $T<>()", mapClassName, classTypeName, contextTypeName,
                        TypeName.get(IdentityHashMap.class))
                .returns(ParameterizedTypeName.get(mapClassName, classTypeName, contextTypeName));
    }

    @Override
    public void write(MetacodeContext context) {
        String master = context.masterElement().toString();
        String metacode = MetacodeUtils.getMetacodeOf(env.getElementUtils(), master);
        String annotations = Joiner.on(",").join(
                Iterables.transform(context.metacodeAnnotations(), new Function<Class<? extends Annotation>, String>() {
                    @Override
                    public String apply(Class<? extends Annotation> input) {
                        return "\n\t\t\t" + input.getCanonicalName() + ".class";
                    }
                }));
        methodBuilder.addCode("result.put($L.class,\n" +
                        "\tnew $T(\n" +
                        "\t\t$L.class,\n" +
                        "\t\tnew org.javameta.util.Provider<$L>() {\n" +
                        "\t\t\tpublic $L get() {\n" +
                        "\t\t\t\treturn new $L();\n" +
                        "\t\t}},\n" +
                        "\t\tnew Class[] {$L\n\t\t}));\n",
                master, TypeName.get(MapMetasitoryContainer.Context.class), master, metacode, metacode, metacode, annotations);
    }

    @Override
    public void close() {
        String metasitoryPackage = env.getOptions().get("jetaMapMetasitoryPackage");
        if (metasitoryPackage == null) {
            logger.debug("jetaMapMetasitoryPackage not present. root package is used");
            metasitoryPackage = "";
        }

        methodBuilder.addStatement("return result");
        typeBuilder.addMethod(methodBuilder.build());

        JavaFile javaFile = JavaFile.builder(metasitoryPackage, typeBuilder.build()).build();
        Writer out = null;
        try {
            String fileName = metasitoryPackage.isEmpty() ?
                    "MetasitoryContainer" : metasitoryPackage + ".MetasitoryContainer";
            logger.debug("writing metasitory to " + fileName);

            JavaFileObject sourceFile = env.getFiler().createSourceFile(fileName);
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
