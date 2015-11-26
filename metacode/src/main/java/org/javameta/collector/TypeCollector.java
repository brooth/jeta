package org.javameta.collector;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Target(ElementType.TYPE)
public @interface TypeCollector {
    Class<? extends Annotation>[] value();
}
