package com.github.brooth.metacode.collector;

import com.github.brooth.metacode.MasterMetacode;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 
 */
public interface TypeCollectorMetacode extends MasterMetacode {
    List<Class> getTypeCollection(Class<? extends Annotation> clazz);
}
