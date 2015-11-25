package com.github.brooth.metacode.collector;

import com.github.brooth.metacode.MasterMetacode;

import java.lang.annotation.Annotation;
import java.util.List;
import com.github.brooth.metacode.util.Provider;

/**
 * 
 */
public interface ObjectCollectorMetacode extends MasterMetacode {
    List<Provider<?>> getObjectCollection(Class<? extends Annotation> clazz);
}
