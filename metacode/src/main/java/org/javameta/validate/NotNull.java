package org.javameta.validate;

import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class NotNull implements Validator {

    @Override
    public void validate(Object object, String name, List<String> errors) {
        if (object == null)
            errors.add(name + " is null");
    }
}
