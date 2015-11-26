package org.javameta.validate;

import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class NotBlank implements Validator {

    @Override
    public void validate(Object object, String fieldName, List<String> errors) {
        boolean result;
        if (object == null) {
            result = true;

        } else if (object instanceof CharSequence) {
            result = object.toString().trim().isEmpty();

        } else {
            throw new IllegalArgumentException("Can't check '" + fieldName + "' is blank");
        }

        if (result)
            errors.add(fieldName + " is blank");
    }
}
