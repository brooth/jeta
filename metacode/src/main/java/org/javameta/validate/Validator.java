package org.javameta.validate;

import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Validator {
    void validate(Object object, String fieldName, List<String> errors);
}
