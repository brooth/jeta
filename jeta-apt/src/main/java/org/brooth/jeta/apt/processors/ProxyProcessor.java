/*
 * Copyright 2016 Oleg Khalidov
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

package org.brooth.jeta.apt.processors;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.proxy.Proxy;
import org.brooth.jeta.proxy.ProxyMetacode;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ProxyProcessor extends AbstractProcessor {

    public ProxyProcessor() {
        super(Proxy.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        ClassName masterClassName = ClassName.get(context.metacodeContext().masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(ProxyMetacode.class), masterClassName));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("applyProxy")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(masterClassName, "master")
                .addParameter(Object.class, "real", Modifier.FINAL);

        for (Element element : context.elements()) {
            String realFieldName = element.getSimpleName().toString();
            ClassName realClassName = ClassName.bestGuess(element.asType().toString());
            String proxyClassNameStr = String.valueOf(MetacodeUtils.getAnnotationValue(element, annotationElement, "value"));
            if (proxyClassNameStr == null)
                throw new ProcessingException("Failed to process " + element.toString() + ", check its source code for compilation errors");
            ClassName proxyClassName = ClassName.bestGuess(proxyClassNameStr);
            TypeElement proxyTypeElement = processingContext.processingEnv().getElementUtils().getTypeElement(proxyClassNameStr);

            TypeSpec.Builder proxyTypeSpecBuilder = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(proxyClassName)
                    .addMethod(MethodSpec.methodBuilder("real")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(realClassName)
                            .addStatement("return ($T) real", realClassName)
                            .build());

            TypeElement realTypeElement = (TypeElement) processingContext.processingEnv().getTypeUtils().asElement(element.asType());
            Set<ExecutableElement> toImplementMethods = new HashSet<>();
            for (Element subElement : ((TypeElement) realTypeElement).getEnclosedElements()) {
                if (subElement.getKind() == ElementKind.METHOD)
                    toImplementMethods.add((ExecutableElement) subElement);
            }

            for (final Element subElement : ((TypeElement) proxyTypeElement).getEnclosedElements()) {
                if (subElement.getKind() == ElementKind.METHOD) {
                    // todo: iterator.remove();
                    toImplementMethods = Sets.filter(toImplementMethods, new Predicate<ExecutableElement>() {
                        public boolean apply(ExecutableElement input) {
                            return !input.toString().equals(subElement.toString());
                        }
                    });
                }
            }

            for (ExecutableElement method : toImplementMethods) {
                TypeMirror[] params = new TypeMirror[method.getParameters().size()];
                String[] values = new String[params.length];
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
                    .addStatement("return true")
                    .endControlFlow();
        }

        methodBuilder.addStatement("return false");
        builder.addMethod(methodBuilder.build());
        return false;
    }
}
