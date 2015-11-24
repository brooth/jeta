package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;

import java.lang.annotation.Annotation;
import java.util.List;

/**
* @author khalidov
* @version $Id$
*/
public interface TypeCollectorMetacode extends MasterMetacode {
    List<Class> getTypeCollection(Class<? extends Annotation> clazz);
}
