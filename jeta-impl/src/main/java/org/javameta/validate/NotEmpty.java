/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.javameta.validate;

import java.util.Collection;
import java.util.Map;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class NotEmpty implements Validator {

    private String fieldName;

    @Override
    public boolean validate(Object master, Object field, String fieldName) {
        this.fieldName = fieldName;

        if (field == null)
            return false;

        if (field instanceof String)
            return !field.toString().isEmpty();

        if (field instanceof Object[])
            return ((Object[]) field).length > 0;

        if ((field instanceof Collection))
            return !((Collection) field).isEmpty();

        if ((field instanceof Map))
            return !((Map) field).isEmpty();

        throw new ValidationException("Can't check '" + field.getClass().getCanonicalName() + "' is empty");
    }

    @Override
    public String describeError() {
        return fieldName + " is empty";
    }
}
