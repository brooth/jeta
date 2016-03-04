/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brooth.jeta.validate;

import org.brooth.jeta.MasterController;
import org.brooth.jeta.metasitory.Metasitory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ValidationController extends MasterController<Object, ValidatorMetacode<Object>> {

    public ValidationController(Metasitory metasitory, Object master) {
        super(metasitory, master, Validate.class);
    }

    public void validate() throws ValidationException {
        List<String> errors = validateSafe();
        if (!errors.isEmpty())
            throw new ValidationException(errors);
    }

    public List<String> validateSafe() {
        List<String> errors = new ArrayList<String>();
        for (ValidatorMetacode<Object> metacode : metacodes)
            errors.addAll(metacode.applyValidation(master));

        return errors;
    }
}
