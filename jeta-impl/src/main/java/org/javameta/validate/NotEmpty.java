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

package org.javameta.validate;

import java.util.Collection;
import java.util.Map;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class NotEmpty implements Validator {

    @Override
    public boolean validate(Object object, String name) {
        if (object == null)
            return false;

        if (object instanceof String)
            return !object.toString().isEmpty();

        if (object instanceof Object[])
            return ((Object[]) object).length > 0;

        if ((object instanceof Collection))
            return !((Collection) object).isEmpty();

        if ((object instanceof Map))
            return !((Map) object).isEmpty();

        throw new ValidationException("Can't check '" + object.getClass().getCanonicalName() + "' is empty");
    }

    @Override
    public String describeError(Object object, String name) {
        return name + " is empty";
    }
}
