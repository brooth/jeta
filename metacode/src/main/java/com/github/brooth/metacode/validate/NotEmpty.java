package com.github.brooth.metacode.validate;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * 
 */
public class NotEmpty implements Validator {

    @Override
    public void validate(Object object, String name, @Nullable String message) throws ValidationException {
        if (object == null)
            throw new ValidationException(message != null ? message : name + " is null");

        if (object instanceof String && object.toString().isEmpty())
            throw new ValidationException(message != null ? message : name + " is empty");

        if (object instanceof Object[] && ((Object[]) object).length < 1)
            throw new ValidationException(message != null ? message : name + " is empty");

		if (object instanceof Collection && ((Collection) object).isEmpty())
            throw new ValidationException(message != null ? message : name + " is empty");

        throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is empty");
    }
}
