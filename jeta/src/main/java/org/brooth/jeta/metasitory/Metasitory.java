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

import org.brooth.jeta.Metacode;

import java.util.Collection;

/**
 * Implementations should store version of criteria (@see Criteria.VERSION)
 * and throw IllegalArgumentException if not compatible with
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Metasitory {
    Collection<Metacode<?>> search(Criteria criteria);

    void add(Metasitory other);
}

