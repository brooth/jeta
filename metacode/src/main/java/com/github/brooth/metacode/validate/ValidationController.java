package com.github.brooth.metacode.validate;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ValidationController extends MasterController<Object, ValidatorMetacode<Object>> {

    public ValidationController(Metasitory metasitory, Object master) {
        super(metasitory, master, Validate.class);
    }

    public void validate() throws ValidationException {
        Set<String> errors = validateSafe();
        if (!errors.isEmpty())
            throw new ValidationException(errors);
    }

    public Set<String> validateSafe() {
        Set<String> errors = new HashSet<>();
        for (ValidatorMetacode<Object> metacode : metacodes)
            errors.addAll(metacode.applyValidation(master));

        return errors;
    }

}
