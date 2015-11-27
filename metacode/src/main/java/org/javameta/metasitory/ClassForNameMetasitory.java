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

package org.javameta.metasitory;

import org.javameta.MasterMetacode;

import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ClassForNameMetasitory implements Metasitory {

    @Override
    public List<MasterMetacode> search(Criteria c) {
        // todo: support
        if (c.getMasterEqDeep() != null)
            throw new UnsupportedOperationException("Criteria.masterAssignableFrom not supported. Criteria.masterEq only.");
        if (c.getUsesAny() != null)
            throw new UnsupportedOperationException("Criteria.usesAny not supported. Criteria.masterEq only.");
        if (c.getUsesAll() != null)
            throw new UnsupportedOperationException("Criteria.usesAll not supported. Criteria.masterEq only.");
        if (c.getMasterEq() == null)
            throw new UnsupportedOperationException("Criteria.masterEq not present.");

        Class<?> metacodeClass;
        try {
            metacodeClass = Class.forName(c.getMasterEq().getName() + "_Metacode");

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class " + c.getMasterEq(), e);
        }

        try {
            MasterMetacode<?> instance = (MasterMetacode) metacodeClass.newInstance();
            return Collections.<MasterMetacode>singletonList(instance);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initiate class " + metacodeClass, e);
        }
    }
}

