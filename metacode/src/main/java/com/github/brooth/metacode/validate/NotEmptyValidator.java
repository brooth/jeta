package com.github.brooth.metacode.validate;

/**
 * @author khalidov
 * @version $Id$
 */
public class NotEmptyValidator implements Validator {

    @Override
    public void validate(Object object, String name) throws AssertionError {
        if (object == null)
            throw new AssertionError(name + " is null");

        if (object instanceof String && object.toString().isEmpty())
            throw new AssertionError(name + " is empty");

        if (object instanceof Object[] && ((Object[]) object).length < 1)
            throw new AssertionError(name + " is empty");

        throw new IllegalArgumentException("Can't check '" + object.getClass().getCanonicalName() + "' is empty");
    }
}
