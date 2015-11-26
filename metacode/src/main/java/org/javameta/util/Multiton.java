package org.javameta.util;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public @interface Multiton {
    String staticConstructor() default "";
}
