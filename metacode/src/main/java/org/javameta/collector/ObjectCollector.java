package org.javameta.collector;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
public @interface ObjectCollector {

    Class<? extends Annotation>[] value();

    String staticConstructior() default "";
}
