package com.github.brooth.metacode.validate;

import java.util.Set;

/**
 * 
 */
public class NotNegative implements Validator {

    @Override
    public void validate(Object object, String name, Set<String> errors) {
        if(!(object instanceof Number))
            throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is negative");

        if (((Number) object).doubleValue() < 0)
            errors.add(name + " is negative");
    }
}
