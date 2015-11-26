package org.javameta.collector;

import org.javameta.util.Provider;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface ObjectCollectorMetacode {
    List<Provider<?>> getObjectCollection(Class<? extends Annotation> annotation);
}
