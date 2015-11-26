package org.javameta.collector;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface TypeCollectorMetacode {
    List<Class> getTypeCollection(Class<? extends Annotation> annotation);
}
