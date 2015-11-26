package org.javameta.validate;

import com.google.common.base.Joiner;

import java.util.List;

/**
 *
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(List<String> errors) {
		super(Joiner.on("; ").join(errors));
    }
}

