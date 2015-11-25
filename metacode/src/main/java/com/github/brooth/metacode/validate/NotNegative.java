package com.github.brooth.metacode.validate;

import java.util.List;

/**
 * 
 */
public class NotNegative implements Validator {

    @Override
    public void validate(Object object, String name, List<String> errors) {
        if(object == null)
            throw new NullPointerException("Object is null");
        if(!(object instanceof Number))
            throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is negative");

        if (((Number) object).doubleValue() < 0)
            errors.add(name + " is negative");
    }
}
