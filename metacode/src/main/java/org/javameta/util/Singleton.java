package org.javameta.util;

public @interface Singleton {
    String staticConstructor() default "";
}
