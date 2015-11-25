package com.github.brooth.metacode.validate;

import com.google.common.base.Joiner;

import java.util.Set;

/**
 *
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(Set<String> errors) {
		super(Joiner.on(", ").join(errors));
    }
}

