package com.github.brooth.metacode.validate;

/**
 *
 */
public @interface Validate {
    Class<? extends IValidator>[] value() default {};
}
