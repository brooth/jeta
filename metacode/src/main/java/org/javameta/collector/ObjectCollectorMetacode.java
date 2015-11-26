package org.javameta.collector;

import org.javameta.MasterMetacode;
import org.javameta.util.Provider;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 */
public interface ObjectCollectorMetacode extends MasterMetacode {
    List<Provider<?>> getObjectCollection(Class<? extends Annotation> clazz);
}
