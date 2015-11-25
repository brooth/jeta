package com.github.brooth.metacode.validate;

import java.util.List;

/**
 *
 */
public class IsNull implements Validator {

    @Override
    public void validate(Object object, String name, List<String> errors) {
        if (object != null)
            errors.add(name + " is not null");
    }
}
