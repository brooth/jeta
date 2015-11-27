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
import org.javameta.validate.MetaValidator;
import org.javameta.validate.Validate;
import org.javameta.validate.ValidatorMetacode;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ValidateProcessor extends SimpleProcessor {

    public ValidateProcessor() {
        super(Validate.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ValidatorMetacode.class), masterClassName));

        ParameterizedTypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        ParameterizedTypeName arrayListTypeName = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get(String.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyValidation")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(listTypeName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addStatement("$T errors = new $T()", listTypeName, arrayListTypeName);

        Elements elementUtils = env.processingEnv().getElementUtils();
        for (Element element : env.elements()) {
            String fieldNameStr = element.getSimpleName().toString();

            final Validate annotation = element.getAnnotation(Validate.class);
            List<String> validators = MetacodeUtils.extractClassesNames(new Runnable() {
                @Override
                public void run() {
                    annotation.value();
                }
            });
            for (String validatorClassNameStr : validators) {
                TypeElement validatorTypeElement = elementUtils.getTypeElement(validatorClassNameStr);
                TypeMirror validatorTypeMirror = validatorTypeElement.asType();
                // Class Validator
                if (validatorTypeElement.getKind() == ElementKind.CLASS) {
                    methodBuilder.addStatement("new $T().validate(master.$L, $S, errors)",
                            TypeName.get(validatorTypeMirror), fieldNameStr, fieldNameStr);

                    // MetacodeValidator
                } else {
                    MetaValidator metaValidator = validatorTypeElement.getAnnotation(MetaValidator.class);
                    if (metaValidator == null)
                        throw new IllegalArgumentException("Not valid Validator usage. '" + validatorClassNameStr
                                + "' must be implementation of Validator"
                                + " or interface annotated with org.javameta.validate.MetacodeValidator");

                    String expression = metaValidator.emitExpression().replaceAll("\\$m", "master");
                    String error = metaValidator.emitError()
                            .replaceAll("\\$m", "master")
                            .replaceAll("\\$\\{([^}]*)}", "\" + $1 + \"");

                    methodBuilder.beginControlFlow("if(!($L)) ", expression)
                            .addStatement("errors.add(\"$L\")", error)
                            .endControlFlow();
                }
            }
        }
        methodBuilder.addStatement("return errors");
        builder.addMethod(methodBuilder.build());

        return false;
    }
}

