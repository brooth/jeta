package org.javameta.collector;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 */
public interface TypeCollectorMetacode {
    List<Class> getTypeCollection(Class<? extends Annotation> annotation);
}
