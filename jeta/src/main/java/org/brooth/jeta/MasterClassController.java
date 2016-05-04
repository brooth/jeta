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

package org.brooth.jeta;

import org.brooth.jeta.metasitory.Criteria;
import org.brooth.jeta.metasitory.Metasitory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @param <M> master's class
 * @param <C> metacode extension
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class MasterClassController<M, C> {

    protected Metasitory metasitory;
    protected Class<? extends M> masterClass;
    protected Collection<C> metacodes;
    protected Set<Class<? extends Annotation>> annotations;
    protected boolean deep;

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, Class<? extends Annotation> annotation) {
        this(metasitory, masterClass, Collections.<Class<? extends Annotation>>singleton(annotation), true);
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, Class<? extends Annotation> annotation, boolean deep) {
        this(metasitory, masterClass, Collections.<Class<? extends Annotation>>singleton(annotation), deep);
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, Set<Class<? extends Annotation>> annotations) {
        this(metasitory, masterClass, annotations, true);
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, Set<Class<? extends Annotation>> annotations, boolean deep) {
        this.metasitory = metasitory;
        this.masterClass = masterClass;
        this.annotations = annotations;
        this.deep = deep;
        searchMetacodes(metasitory);
    }

    @SuppressWarnings("unchecked")
    protected void searchMetacodes(Metasitory metasitory) {
        assert metasitory != null;
        this.metacodes = (Collection<C>) metasitory.search(criteria());
    }

    protected Criteria criteria() {
        Criteria.Builder builder = new Criteria.Builder();
        if (deep)
            builder.masterEqDeep(masterClass);
        else
            builder.masterEq(masterClass);
        return builder.usesAny(annotations).build();
    }
}

