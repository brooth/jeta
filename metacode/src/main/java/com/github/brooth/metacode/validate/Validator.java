package com.github.brooth.metacode.validate;

import java.util.Set;

/**
 *
 */
public interface Validator extends IValidator {
    void validate(Object object, String fieldName, Set<String> errors);
}
