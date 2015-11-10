package com.github.brooth.metacode.validate;

/**
 * 
 */
public class NotNegative implements Validator {

    @Override
    public void validate(Object object, String name) throws ValidationException {
        if (object instanceof Number && ((Number) object).doubleValue() < 0)
            throw new ValidationException(name + " is negative");

        throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is negative");
    }
}
