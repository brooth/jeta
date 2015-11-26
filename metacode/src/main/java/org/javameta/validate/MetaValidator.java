package org.javameta.validate;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public @interface MetaValidator {
    String emitExpression();

    String emitError();
}
