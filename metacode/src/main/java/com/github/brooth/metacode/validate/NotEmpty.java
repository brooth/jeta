package com.github.brooth.metacode.validate;

import java.util.Collection;
import java.util.Set;

/**
 *
 */
public class NotEmpty implements Validator {

    @Override
    public void validate(Object object, String name, Set<String> errors) {
        boolean result;
        if (object == null) {
            result = true;

        } else if (object instanceof String) {
            result = object.toString().isEmpty();

        } else if (object instanceof Object[]) {
            result = ((Object[]) object).length < 1;

        } else if ((object instanceof Collection)) {
            result = ((Collection) object).isEmpty();

        } else {
            throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is empty");
        }

        if (result)
            errors.add(name + " is empty");
    }
}
