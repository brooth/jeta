package com.github.brooth.metacode.validate;

/**
 * @author khalidov
 * @version $Id$
 */
public @interface Validate {
    Class<? extends Validator>[] value() default {};

    String expression() default "";
}