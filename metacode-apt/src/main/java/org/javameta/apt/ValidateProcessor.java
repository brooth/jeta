package org.javameta.apt;

import com.squareup.javapoet.*;
import org.javameta.validate.MetaValidator;
import org.javameta.validate.Validate;
import org.javameta.validate.ValidatorMetacode;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

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

        ParameterizedTypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        ParameterizedTypeName arrayListTypeName = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get(String.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyValidation")
                .addModifiers(Modifier.PUBLIC)
                .returns(listTypeName)
                .addParameter(masterClassName, "master", Modifier.FINAL)
                .addStatement("$T errors = new $T()", listTypeName, arrayListTypeName);

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

