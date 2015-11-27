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

package org.javameta.apt.processors;

import com.squareup.javapoet.*;
import org.javameta.apt.MetacodeContext;
import org.javameta.apt.MetacodeUtils;
import org.javameta.apt.ProcessorEnvironment;
import org.javameta.log.Log;
import org.javameta.log.LogMetacode;
import org.javameta.log.NamedLogger;
import org.javameta.util.Provider;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(LogMetacode.class), masterClassName));

        TypeName providerTypeName = ParameterizedTypeName.get(ClassName.get(Provider.class),
                WildcardTypeName.subtypeOf(NamedLogger.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyLogger")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(masterClassName, "master")
                .addParameter(providerTypeName, "provider");

        for (Element element : env.elements()) {
            String fieldName = element.getSimpleName().toString();
            String loggerName = element.getAnnotation(Log.class).value();
            if (loggerName.isEmpty())
                loggerName = MetacodeUtils.typeElementOf(element).getSimpleName().toString();

            methodBuilder
                    .addStatement("master.$L = ($T) provider.get()",
                            fieldName, TypeName.get(element.asType()))
                    .addStatement("master.$L.setName($S)",
                            fieldName, loggerName);
        }

        builder.addMethod(methodBuilder.build());
        return false;
    }
}
