package com.github.brooth.metacode.util;

public @interface Multiton {
    String staticConstructor() default "";
}
