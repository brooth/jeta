package com.github.brooth.metacode.apt;

import com.github.brooth.metacode.proxy.Proxy;
import com.github.brooth.metacode.proxy.ProxyMetacode;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author khalidov
 * @version $Id$
 */
public class ProxyMetaProcessor extends SimpleProcessor {

    public ProxyMetaProcessor() {
        super(Proxy.class);
    }

    @Override
    public boolean process(RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        MetacodeContext context = ctx.metacodeContext;
        ClassName masterClassName = ClassName.bestGuess(context.getMasterCanonicalName());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ProxyMetacode.class), masterClassName));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyProxy")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(masterClassName, "master")
                .addParameter(Object.class, "real", Modifier.FINAL);

        for (Element element : ctx.elements) {
            String realFieldName = element.getSimpleName().toString();
            ClassName realClassName = ClassName.bestGuess(element.asType().toString());
            final Proxy annotation = element.getAnnotation(Proxy.class);
            String proxyClassNameStr = MetacodeUtils.extractClassName(new Runnable() {
                @Override
                public void run() {
                    annotation.value();
                }
            });
            TypeElement proxyTypeElement = ctx.env.getElementUtils().getTypeElement(proxyClassNameStr);
            ClassName proxyClassName = ClassName.bestGuess(proxyClassNameStr);

            TypeSpec.Builder proxyTypeSpecBuilder = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(proxyClassName)
                    .addMethod(MethodSpec.methodBuilder("real")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(realClassName)
                            .addStatement("return ($T) real", realClassName)
                            .build());

            TypeElement realTypeElement = (TypeElement) ctx.env.getTypeUtils().asElement(element.asType());
            Set<ExecutableElement> toImplementMethods = new HashSet<>();
            for (Element subElement : ((TypeElement) realTypeElement).getEnclosedElements()) {
                if (subElement.getKind() == ElementKind.METHOD)
                    toImplementMethods.add((ExecutableElement) subElement);
            }

            for (final Element subElement : ((TypeElement) proxyTypeElement).getEnclosedElements()) {
                if (subElement.getKind() == ElementKind.METHOD) {
                    // todo: iterator.remove();
                    toImplementMethods = Sets.filter(toImplementMethods, new Predicate<ExecutableElement>() {
                        @Override
                        public boolean apply(ExecutableElement input) {
                            return !input.toString().equals(subElement.toString());
                        }
                    });
                }
            }

            for (ExecutableElement method : toImplementMethods) {
                TypeMirror[] params = new TypeMirror[method.getParameters().size()];
                String[] values = new String[params.lenght];
                int pi = 0;
                for (VariableElement param : method.getParameters()) {
                    params[pi] = param.asType();
                    values[pi] = param.getSimpleName().toString();
                }

                TypeMirror returnType = method.getReturnType();
                String methodNameStr = method.getSimpleName().toString();
                MethodSpec.Builder methodImplSpecBuilder = MethodSpec.methodBuilder(methodNameStr)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(returnType))
                        .addStatement((returnType.toString().equals("void") ? "" : "return ") + "real().$L($L)",
                                methodNameStr, Joiner.on(", ").join(values));

                for (int i = 0; i < params.length; i++)
                    methodImplSpecBuilder.addParameter(TypeName.get(params[i]), values[i]);

                proxyTypeSpecBuilder.addMethod(methodImplSpecBuilder.build());
            }

            methodBuilder
                    .beginControlFlow("if (real == master.$L)", realFieldName)
                    .addStatement("master.$L = $L", realFieldName, proxyTypeSpecBuilder.build())
                    .addStatement("return")
                    .endControlFlow();
        }

        methodBuilder.addStatement("throw new IllegalArgumentException(real.getClass() + \" not valid object for proxy wrapping. " +
                "Is its field annotated with @Proxy?\")");
        builder.addMethod(methodBuilder.build());
        return false;
    }
}
