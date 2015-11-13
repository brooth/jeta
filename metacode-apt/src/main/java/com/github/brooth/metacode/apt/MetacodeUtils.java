package com.github.brooth.metacode.apt;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author khalidov
 * @version $Id$
 */
public class MetacodeUtils {

    public String getMetacodeOf(Elements elementsUtils, String masterCanonicalName) {
        TypeElement typeElement = elementsUtils.getTypeElement(masterCanonicalName);
        Object packageName = elementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
        return packageName + "." + masterCanonicalName.replace(packageName + ".", "")
                .replaceAll("\\.", "\\$") + MetacodeProcessor.METACODE_CLASS_POSTFIX;
    }

    public static TypeElement typeOf(Element element) {
        return element.getKind().isClass() || element.getKind().isInterface()
                ? (TypeElement) element : (TypeElement) element.getEnclosingElement();
    }
}
