package com.github.brooth.metacode.util;

public @interface Singleton {
    String staticConstructor() default "";
}
