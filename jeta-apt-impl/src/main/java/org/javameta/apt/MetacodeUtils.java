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

package org.javameta.apt;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetacodeUtils {

    public static String getMetacodeOf(Elements elementsUtils, String canonicalName) {
        TypeElement typeElement = elementsUtils.getTypeElement(canonicalName);
        Object packageName = elementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
        return packageName + "." + canonicalName.replace(packageName + ".", "")
                .replaceAll("\\.", "_") + MetacodeProcessor.METACODE_CLASS_POSTFIX;
    }

    public static TypeElement sourceElementOf(Element element) {
        Element sourceElement = element;
        while (sourceElement.getEnclosingElement() != null && sourceElement.getEnclosingElement().getKind().isClass()) {
            sourceElement = sourceElement.getEnclosingElement();
        }
        return (TypeElement) sourceElement;
    }

    public static TypeElement typeElementOf(Element element) {
        return element.getKind().isClass() || element.getKind().isInterface()
                ? (TypeElement) element : (TypeElement) element.getEnclosingElement();
    }

    public static String extractClassName(Runnable invoke) {
        try {
            invoke.run();
            throw new IllegalArgumentException("invoke doesn't throws MirroredTypesException");

        } catch (MirroredTypesException e) {
            return e.getTypeMirrors().get(0).toString();
        }
    }

    public static List<String> extractClassesNames(Runnable invoke) {
        try {
            invoke.run();
            throw new IllegalArgumentException("invoke doesn't throws MirroredTypesException");

        } catch (MirroredTypesException e) {
            return Lists.transform(e.getTypeMirrors(), new Function<TypeMirror, String>() {
                @Override
                public String apply(TypeMirror input) {
                    return input.toString();
                }
            });
        }
    }
}
