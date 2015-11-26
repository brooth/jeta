package org.javameta.observer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Target(ElementType.METHOD)
public @interface Observer {
    Class<?>[] value();
}
