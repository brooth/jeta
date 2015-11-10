package com.github.brooth.metacode.validate;

import javax.annotation.Nullable;

/**
 *  
 */
public class NotNull implements Validator {

    @Override
    public void validate(Object object, String name, @Nullable String message) {
        if (object == null)
            throw new ValidationException(message != null ? message : name + " is null");
    }
}
