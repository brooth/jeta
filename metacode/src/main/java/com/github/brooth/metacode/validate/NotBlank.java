package com.github.brooth.metacode.validate;

/**
 * 
 */
public class NotBlank implements Validator {

    @Override
    public void validate(Object object, String name) throws ValidationException {
        if (object instanceof String && object.toString().trim().isEmpty())
            throw new ValidationException(name + " is blank");

        throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is blank");
    }
}
