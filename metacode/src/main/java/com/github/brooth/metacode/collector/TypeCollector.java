package com.github.brooth.metacode.collector;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
public @interface TypeCollector {
    Class<? extends Annotation>[] value();
}
