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

package org.brooth.jeta.apt;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetacodeUtils {

    public static String toMetacodeName(String canonicalName) {
        return toMetaName(canonicalName, JetaProcessor.METACODE_CLASS_POSTFIX);
    }

    public static String toMetaName(String canonicalName, String postfix) {
        ClassName className = ClassName.bestGuess(canonicalName);
        return className.packageName() + "." + Joiner.on('_').join(className.simpleNames()) + postfix;
    }

    public static String toSimpleMetacodeName(String canonicalName) {
        return toSimpleMetaName(canonicalName, JetaProcessor.METACODE_CLASS_POSTFIX);
    }

    public static String toSimpleMetaName(String canonicalName, String postfix) {
        String metacodeCanonicalName = toMetaName(canonicalName, postfix);
        int i = metacodeCanonicalName.lastIndexOf('.');
        return i >= 0 ? metacodeCanonicalName.substring(i + 1) : metacodeCanonicalName;
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
            throw new IllegalArgumentException("invoke doesn't throw MirroredTypesException");

        } catch (MirroredTypesException e) {
            return e.getTypeMirrors().get(0).toString();
        }
    }

    public static List<String> extractClassesNames(Runnable invoke) {
        try {
            invoke.run();
            throw new IllegalArgumentException("invoke doesn't throw MirroredTypesException");

        } catch (MirroredTypesException e) {
            return Lists.transform(e.getTypeMirrors(), new Function<TypeMirror, String>() {
                public String apply(TypeMirror input) {
                    return input.toString();
                }
            });
        }
    }
}
