package com.github.brooth.metacode.validate;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 *
 */
public class ValidationController extends MasterController<Object, ValidationController.ValidatorMetacode<Object>> {

    public ValidationController(Metasitory metasitory, Object master) {
        super(metasitory, master, Validate.class);
    }

    public void validate() {
        for (ValidatorMetacode<Object> metacode : metacodes)
            metacode.validate(master);
    }

    public interface ValidatorMetacode<M> extends MasterMetacode<M> {
        void validate(M master);
    }
}
