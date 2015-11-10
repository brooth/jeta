package com.github.brooth.metacode.validate;

import javax.annotation.Nullable;

/**
 * 
 */
public interface Validator {
    void validate(Object object, String name, @Nullable String message) throws ValidationException;
}
