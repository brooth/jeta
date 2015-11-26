package org.javameta.validate;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public @interface Validate {
    Class<? extends Validator>[] value() default {};
}
