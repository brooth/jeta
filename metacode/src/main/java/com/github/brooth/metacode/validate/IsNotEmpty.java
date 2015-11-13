package com.github.brooth.metacode.validate;

import java.util.Collection;

/**
 * 
 */
public class IsNotEmpty implements Validator {

    @Override
    public void validate(Object object, String name) throws ValidationException {
        if (object == null)
            throw new ValidationException(name + " is null");

        if (object instanceof String && object.toString().isEmpty())
            throw new ValidationException(name + " is empty");

        if (object instanceof Object[] && ((Object[]) object).length < 1)
            throw new ValidationException(name + " is empty");

		if (object instanceof Collection && ((Collection) object).isEmpty())
            throw new ValidationException(name + " is empty");

        throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is empty");
    }
}
