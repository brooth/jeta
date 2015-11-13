package com.github.brooth.metacode.observer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.METHOD)
public @interface Observer {
    Class<?>[] value();
}
