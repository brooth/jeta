package com.github.brooth.metacode.validate;

/**
 * @author khalidov
 * @version $Id$
 */
public class NotNullValidator implements Validator {
    @Override
    public void validate(Object object, String name) {
        if (object == null)
            throw new AssertionError(name + " is null");
    }
}
