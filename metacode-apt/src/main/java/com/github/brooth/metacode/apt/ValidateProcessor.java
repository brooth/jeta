package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.validate.MetaValidator;
import com.github.brooth.metacode.validate.Validate;
import com.github.brooth.metacode.validate.ValidatorMetacode;
import com.squareup.javapoet.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ValidateProcessor extends SimpleProcessor {

    public ValidateProcessor() {
        super(Validate.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        MetacodeContext context = ctx.metacodeContext;
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ValidatorMetacode.class), masterClassName));

        ParameterizedTypeName setTypeName = ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));
        ParameterizedTypeName hashSetTypeName = ParameterizedTypeName.get(ClassName.get(HashSet.class), ClassName.get(String.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyValidation")
                .addModifiers(Modifier.PUBLIC)
                .returns(setTypeName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addStatement("$T errors = new $T()", setTypeName, hashSetTypeName);

        Types typeUtils = ctx.env.getTypeUtils();
        Elements elementUtils = ctx.env.getElementUtils();

        for (Element element : ctx.elements) {
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
                // Object Validator
                if (typeUtils.isAssignable(validatorTypeMirror,
                        elementUtils.getTypeElement("com.github.brooth.metacode.validate.Validator").asType())) {
                    methodBuilder.addStatement("new $T().validate(master.$L, $S, errors)",
                            TypeName.get(validatorTypeMirror), fieldNameStr, fieldNameStr);

                    // MetacodeValidator
                } else {
                    MetaValidator metaValidator = validatorTypeElement.getAnnotation(MetaValidator.class);
                    if (metaValidator == null)
                        throw new IllegalArgumentException("Not valid IValidator usage. '" + validatorClassNameStr
                                + "' must be annotated with @MetacodeValidator or extend Validator");

                    String expression = metaValidator.emitExpression().replaceAll("%m", "master");
                    String error = metaValidator.emitError().replaceAll("%m([a-zA-Z0-9_.]*)", "\" + master$1 + \"");
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

