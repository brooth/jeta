package com.github.brooth.metacode.validate;

/**
 *  
 * 
 */
public @interface Validate {
    Class<? extends Validator>[] value() default {};

    String expression() default "";

    String expressionError() default "";
}
