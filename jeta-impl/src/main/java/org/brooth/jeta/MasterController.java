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

package org.brooth.jeta;

import org.brooth.jeta.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class MasterController<M, C> extends MasterClassController<M, C> {

    protected M master;

    protected MasterController(Metasitory metasitory, M master) {
        this(metasitory, master, null);
    }

    @SuppressWarnings("unchecked")
    public MasterController(Metasitory metasitory, M master, @Nullable Class<? extends Annotation> annotationClass) {
        super(metasitory, (Class<? extends M>) master.getClass(), annotationClass);
        this.master = master;
    }
}

