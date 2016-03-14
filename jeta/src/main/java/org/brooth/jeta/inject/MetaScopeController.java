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
 *
 */

package org.brooth.jeta.inject;

import org.brooth.jeta.MasterController;
import org.brooth.jeta.metasitory.Criteria;
import org.brooth.jeta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaScopeController<S> extends MasterController<S, MetaScopeMetacode<S>> {

    public MetaScopeController(Metasitory metasitory, S scope) {
        super(metasitory, scope);
    }

    @Override
    protected Criteria criteria() {
        Criteria.Builder builder = new Criteria.Builder().masterEq(masterClass);
        builder.usesAny(Scope.class);
        return builder.build();
    }

    public MetaScope<S> get() {
        if (metacodes.isEmpty())
            throw new IllegalArgumentException(masterClass + " is not a meta scope. Put @Scope annotation on it");
        return metacodes.iterator().next().getMetaScope(master);
    }
}
