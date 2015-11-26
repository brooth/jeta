package org.javameta.validate;

/**
 *
 */
public @interface Validate {
    Class<? extends Validator>[] value() default {};
}
