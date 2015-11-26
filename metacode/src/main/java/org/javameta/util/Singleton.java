package org.javameta.util;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public @interface Singleton {
    String staticConstructor() default "";
}
