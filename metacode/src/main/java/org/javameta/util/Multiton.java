package org.javameta.util;

public @interface Multiton {
    String staticConstructor() default "";
}
