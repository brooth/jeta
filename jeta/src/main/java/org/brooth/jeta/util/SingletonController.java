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

package org.brooth.jeta.util;

import com.google.common.collect.Iterables;

import org.brooth.jeta.IMetacode;
import org.brooth.jeta.MasterClassController;
import org.brooth.jeta.metasitory.Criteria;
import org.brooth.jeta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonController<M> extends MasterClassController<M, IMetacode<?>> {

    public SingletonController(Metasitory metasitory, Class<M> masterClass) {
        super(metasitory, masterClass);
    }

    @Override
    protected Criteria criteria() {
       return new Criteria.Builder().masterEq(masterClass).usesAny(Singleton.class).build();
    }

    @SuppressWarnings("unchecked")
    public SingletonMetacode<M> getMetacode() {
        if (metacodes.size() > 1)
            throw new IllegalStateException("More than one metacode returned fot Criteria.masterEq");

        IMetacode<?> singleton = Iterables.getFirst(metacodes, null);
        if (singleton == null)
            throw new IllegalStateException(masterClass.getCanonicalName() + " has not singleton meta code. No @Singleton annotation on it?");

        return ((SingletonMetacode<M>) singleton);
    }
}
