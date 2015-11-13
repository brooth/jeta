package com.github.brooth.metacode.validate;

/**
 *  
 */
public class IsNotNull implements Validator {

    @Override
    public void validate(Object object, String name) {
        if (object == null)
            throw new ValidationException(name + " is null");
    }
}
