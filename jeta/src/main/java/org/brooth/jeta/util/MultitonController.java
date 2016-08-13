/*
 * Copyright 2016 Oleg Khalidov
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

package org.brooth.jeta.util;

import org.brooth.jeta.MasterClassController;
import org.brooth.jeta.Metacode;
import org.brooth.jeta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MultitonController<M> extends MasterClassController<M, Metacode<?>> {

    public MultitonController(Metasitory metasitory, Class<M> masterClass) {
        super(metasitory, masterClass, Multiton.class, false);
    }

    @SuppressWarnings("unchecked")
    public MultitonMetacode<M> getMetacode() {
        if (metacodes.size() > 1)
            throw new IllegalStateException("More than one metacode returned fot Criteria.masterEq");

        Metacode<?> multiton = metacodes.iterator().next();
        if (multiton == null)
            throw new IllegalStateException(masterClass.getCanonicalName() + " has not multiton meta code. No @Multiton annotation on it?");

        return (MultitonMetacode<M>) multiton;
    }
}
