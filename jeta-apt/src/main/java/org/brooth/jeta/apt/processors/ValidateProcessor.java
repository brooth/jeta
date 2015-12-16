/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta.apt.processors;

import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeContext;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessorEnvironment;
import org.brooth.jeta.validate.MetaValidator;
import org.brooth.jeta.validate.Validate;
import org.brooth.jeta.validate.ValidatorMetacode;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ValidateProcessor extends AbstractProcessor {

    public ValidateProcessor() {
        super(Validate.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.get(context.masterElement());
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
        int i = 0;
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
                String validatorVarName = "validator_" + i++;
                TypeElement validatorTypeElement = elementUtils.getTypeElement(validatorClassNameStr);
                TypeName validatorTypeName = TypeName.get(validatorTypeElement.asType());

                // Class Validator
                if (validatorTypeElement.getKind() == ElementKind.CLASS) {
                    methodBuilder
                            .addStatement("$T $L = new $T()", validatorTypeName, validatorVarName, validatorTypeName)
                            .beginControlFlow("if(!($L.validate(master, master.$L, $S)))", validatorVarName, fieldNameStr, fieldNameStr)
                            .addStatement("errors.add($L.describeError())", validatorVarName)
                            .endControlFlow();

                    // MetacodeValidator
                } else {
                    MetaValidator metaValidator = validatorTypeElement.getAnnotation(MetaValidator.class);
                    if (metaValidator == null)
                        throw new IllegalArgumentException("Not valid Validator usage. '" + validatorClassNameStr
                                + "' must be implementation of Validator"
                                + " or interface annotated with org.brooth.jeta.validate.MetacodeValidator");

                    String expression = metaValidator.emitExpression()
                            .replaceAll("\\$f", "master." + fieldNameStr)
                            .replaceAll("\\$m", "master");
                    String error = metaValidator.emitError()
                            .replaceAll("\\$f", "master." + fieldNameStr)
                            .replaceAll("\\$n", fieldNameStr)
                            .replaceAll("\\$m", "master")
                            .replaceAll("\\$\\{([^}]*)}", "\" + ($1) + \"");

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

