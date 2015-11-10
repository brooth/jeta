package com.github.brooth.metacode.validate;

/**
 *  
 */
public class IsNull implements Validator {

    @Override
    public void validate(Object object, String name) {
        if (object != null)
            throw new ValidationException(name + " is not null");
    }
}
