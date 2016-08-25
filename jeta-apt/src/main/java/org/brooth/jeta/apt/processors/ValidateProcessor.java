/*
 * Copyright 2016 Oleg Khalidov
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingContext;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.validate.MetaValidator;
import org.brooth.jeta.validate.Validate;
import org.brooth.jeta.validate.ValidatorMetacode;
import org.brooth.jeta.validate.alias.NotBlank;
import org.brooth.jeta.validate.alias.NotEmpty;
import org.brooth.jeta.validate.alias.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ValidateProcessor extends AbstractProcessor {

    private Map<TypeElement, String> aliases;

    public ValidateProcessor() {
        super(Validate.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);

        aliases = new HashMap<>();
        aliases.put(getTypeElement(NotBlank.class), "org.brooth.jeta.validate.NotBlank");
        aliases.put(getTypeElement(NotEmpty.class), "org.brooth.jeta.validate.NotEmpty");
        aliases.put(getTypeElement(NotNull.class), "org.brooth.jeta.validate.NotNull");

        for (String key : processingContext.processingProperties().stringPropertyNames()) {
            if (key.startsWith("validator.alias.")) {
                String annStr = key.substring("validator.alias.".length());
                String valStr = processingContext.processingProperties().getProperty(key);
                try {
                    TypeElement typeElement = processingContext.processingEnv().getElementUtils().getTypeElement(annStr);
                    if (typeElement == null)
                        throw new IllegalArgumentException("Validator alias " + annStr + " not found");
                    if (typeElement.getKind() != ElementKind.ANNOTATION_TYPE)
                        throw new IllegalArgumentException(annStr + " is not a annotation type.");
                    aliases.put(typeElement, valStr);
                } catch (Exception e) {
                    throw new ProcessingException("Failed to load '" + annStr + "' validator alias.", e);
                }
            }
        }
    }

    @Override
    public Set<TypeElement> collectElementsAnnotatedWith() {
        Set<TypeElement> result = new HashSet<>();
        result.add(getTypeElement(Validate.class));
        result.addAll(aliases.keySet());
        return result;
    }

    protected TypeElement getTypeElement(Class clazz) {
        return processingContext.processingEnv().getElementUtils().getTypeElement(clazz.getCanonicalName());
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
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

        Elements elementUtils = processingContext.processingEnv().getElementUtils();
        int i = 0;
        for (Element element : context.elements()) {
            String fieldNameStr = element.getSimpleName().toString();
            List<?> validatorList = (List<?>) MetacodeUtils.getAnnotationValue(element, annotationElement, "value");
            List<String> validators;
            if (validatorList != null) {
                validators = Lists.transform(validatorList, new Function<Object, String>() {
                    @Override
                    public String apply(Object input) {
                        return input.toString().replace(".class", "");
                    }
                });
            } else {
                validators = new ArrayList<>();
            }

            for (TypeElement alias : aliases.keySet()) {
                if (MetacodeUtils.getAnnotation(element, alias) != null)
                    validators.add(aliases.get(alias));
            }

            for (String validatorClassNameStr : validators) {
                String validatorVarName = "validator_" + i++;
                TypeElement validatorTypeElement = elementUtils.getTypeElement(validatorClassNameStr);
                if (validatorTypeElement == null)
                    throw new ProcessingException("Validator '" + validatorClassNameStr + "' not found");
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

