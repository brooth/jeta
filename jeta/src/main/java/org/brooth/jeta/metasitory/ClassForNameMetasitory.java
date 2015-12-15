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

package org.brooth.jeta.metasitory;

import org.brooth.jeta.IMetacode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ClassForNameMetasitory implements Metasitory {

    @Override
    public List<IMetacode<?>> search(Criteria c) {
        if (c.getUsesAny() != null)
            throw new UnsupportedOperationException("Criteria.usesAny not supported. Criteria.masterEq only.");
        if (c.getUsesAll() != null)
            throw new UnsupportedOperationException("Criteria.usesAll not supported. Criteria.masterEq only.");

        Class<?> masterClass = c.getMasterEq() != null ? c.getMasterEq() : c.getMasterEqDeep();
        Class<?> metacodeClass;
        List<IMetacode<?>> result = new ArrayList<>();
        while (masterClass != null) {
            try {
                metacodeClass = Class.forName(masterClass.getName().replaceAll("\\$", "_") + "_Metacode");

            } catch (ClassNotFoundException e) {
                break;
            }

            try {
                result.add((IMetacode<?>) metacodeClass.newInstance());

            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to initiate class " + metacodeClass, e);
            }

            if(c.getMasterEqDeep() != null)
                masterClass = masterClass.getSuperclass();
            else 
                break;
        }
        return result;
    }
}

