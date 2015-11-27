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

package org.javameta;

import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * @param <M> master's class
 * @param <C> metacode extension
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class MasterClassController<M, C> {

    protected Class<? extends M> masterClass;
    protected Collection<C> metacodes;
    @Nullable
    protected Class<? extends Annotation> annotationClass;

    protected MasterClassController(Metasitory metasitory, Class<? extends M> masterClass) {
        this.masterClass = masterClass;
        searchMetacodes(metasitory);
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, @Nullable Class<? extends Annotation> annotationClass) {
        this(metasitory, masterClass);
        this.annotationClass = annotationClass;
    }

    @SuppressWarnings("unchecked")
    protected void searchMetacodes(Metasitory metasitory) {
        this.metacodes = (Collection<C>) metasitory.search(criteria());
    }

    protected Criteria criteria() {
        Criteria.Builder builder = new Criteria.Builder().masterEqDeep(masterClass);
        if (annotationClass != null)
            builder.usesAny(annotationClass);
        return builder.build();
    }
}

