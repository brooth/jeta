package com.github.brooth.metacode.validate;

import java.util.List;

/**
 *
 */
public interface Validator {
    void validate(Object object, String fieldName, List<String> errors);
}
