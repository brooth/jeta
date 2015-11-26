package org.javameta.collector;

import org.javameta.MasterMetacode;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 */
public interface TypeCollectorMetacode extends MasterMetacode {
    List<Class> getTypeCollection(Class<? extends Annotation> clazz);
}
