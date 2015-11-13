package com.github.brooth.metacode.apt;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author khalidov
 * @version $Id$
 */
public class MetacodeUtils {

    public String getMetacodeOf(String masterFullname) {
        return null;
    }

    public static TypeElement typeOf(Element element) {
        return element.getKind().isClass() || element.getKind().isInterface()
                ? (TypeElement) element : (TypeElement) element.getEnclosingElement();
    }
}
