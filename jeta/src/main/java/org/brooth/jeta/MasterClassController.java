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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.brooth.jeta.metasitory.Criteria;
import org.brooth.jeta.metasitory.Metasitory;

import javax.annotation.Nullable;
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
    @Nullable
    protected Set<Class<? extends Annotation>> annotations;

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass) {
        this(metasitory, masterClass, (Set<Class<? extends Annotation>>) null);
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, Class<? extends Annotation> annotation) {
        this(metasitory, masterClass, Collections.<Class<? extends Annotation>>singleton(annotation));
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, @Nullable Set<Class<? extends Annotation>> annotations) {
        this.metasitory = metasitory;
        this.masterClass = masterClass;
        this.annotations = annotations;
        searchMetacodes(metasitory);
    }

    @SuppressWarnings("unchecked")
    protected void searchMetacodes(Metasitory metasitory) {
        Preconditions.checkNotNull(metasitory, "metasitory");
        this.metacodes = (Collection<C>) metasitory.search(criteria());
    }

    protected Criteria criteria() {
        Criteria.Builder builder = new Criteria.Builder().masterEqDeep(masterClass);
        if (annotations != null)
            builder.usesAny(annotations);
        return builder.build();
    }
}

