package com.github.brooth.metacode.validate;

/**
 *
 */
public interface Validator {
    void validate(Object object, String fieldName) throws ValidationException;
}
