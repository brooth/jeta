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

package org.javameta.util;

import com.google.common.collect.Iterables;
import org.javameta.MasterClassController;
import org.javameta.MasterMetacode;
import org.javameta.metasitory.ClassForNameMetasitory;
import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonController<M> extends MasterClassController<M, MasterMetacode> {

    public SingletonController(Class<? extends M> masterClass) {
        this(new ClassForNameMetasitory(), masterClass);
    }

    public SingletonController(Metasitory metasitory, Class<? extends M> masterClass) {
        super(metasitory, masterClass);
    }

    @Override
    protected Criteria criteria() {
        return new Criteria.Builder().masterEq(masterClass).build();
    }

    public M getInstance() {
        MasterMetacode singleton = Iterables.getFirst(metacodes, null);
        if (singleton == null || !(singleton instanceof SingletonMetacode))
            throw new IllegalStateException(masterClass.getCanonicalName() + " has not singleton meta code. No @Singleton annotation on it?");

        @SuppressWarnings("unchecked")
        M instance = (M) ((SingletonMetacode) singleton).getInstance();
        return instance;
    }

}
