/*
 * Copyright 2016 Oleg Khalidov
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

package org.brooth.jeta.proxy;

import org.brooth.jeta.Metacode;
import org.brooth.jeta.metasitory.Criteria;
import org.brooth.jeta.metasitory.Metasitory;

import java.util.Collection;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ProxyController {

    private ProxyMetacode<Object> metacode;
    private Object master;

    public ProxyController(Metasitory metasitory, Object master) {
        this.master = master;
        search(metasitory);
    }

    @SuppressWarnings("unchecked")
    private void search(Metasitory metasitory) {
        assert metasitory != null;

        Criteria criteria = new Criteria.Builder()
                .masterEq(master.getClass())
                .usesAny(Proxy.class)
                .build();

        Collection<Metacode<?>> metacodes = metasitory.search(criteria);
        if (metacodes.size() > 1)
            throw new IllegalStateException("Metasitory returned more than one masterEq result");
        if (metacodes.size() == 1)
            metacode = (ProxyMetacode<Object>) metacodes.iterator().next();
    }

    public void createProxy(Object real) {
        assert real != null;

        if (metacode == null)
            throw new IllegalStateException("No metacode found to create proxy");

        if (!metacode.applyProxy(master, real))
            throw new IllegalArgumentException(real.getClass() + " not valid object for proxy wrapping. Is its field annotated with @Proxy?");
    }
}
