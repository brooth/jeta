package com.github.brooth.metacode.validate;

/**
 * @author khalidov
 * @version $Id$
 */
public interface Validator {
    void validate(Object object, String name) throws AssertionError;
}
