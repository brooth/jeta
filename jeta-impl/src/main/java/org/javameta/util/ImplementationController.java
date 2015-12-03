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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.javameta.IMetacode;
import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ImplementationController<I> {

    protected Collection<ImplementationMetacode<I>> metacodes;
    protected Class<I> of;

    public ImplementationController(Metasitory metasitory, Class<I> of) {
        this.of = of;
        searchMetacodes(metasitory);
    }

    protected void searchMetacodes(Metasitory metasitory) {
        Collection<IMetacode> allImplementers =
                metasitory.search(new Criteria.Builder().usesAny(Implementation.class).build());

        metacodes = FluentIterable.from(allImplementers)
                .transform(new Function<IMetacode, ImplementationMetacode<I>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public ImplementationMetacode<I> apply(IMetacode input) {
                        return (ImplementationMetacode<I>) input;
                    }
                })
                .filter(new Predicate<ImplementationMetacode>() {
                    @Override
                    public boolean apply(ImplementationMetacode input) {
                        return input.getImplementationOf() == of;
                    }
                })
                .toSortedList(new Comparator<ImplementationMetacode<I>>() {
                    @Override
                    public int compare(ImplementationMetacode<I> o1, ImplementationMetacode<I> o2) {
                        return o1.getImplementationPriority() == o2.getImplementationPriority() ? 0 :
                                o1.getImplementationPriority() > o2.getImplementationPriority() ? -1 : 1;
                    }
                });
    }

    public I getImplementation() {
        ImplementationMetacode<I> first = Iterables.getFirst(metacodes, null);
        if (first == null)
            return null;

        if (metacodes.size() > 1 && first.getImplementationPriority() == Iterables.get(metacodes, 1).getImplementationPriority())
            throw new IllegalStateException("More that one implementation with highest priority " + first.getImplementationPriority());

        return first.getImplementation();
    }

    public Collection<I> getImplementations() {
        return Collections2.transform(metacodes, new Function<ImplementationMetacode<I>, I>() {
            @Override
            public I apply(ImplementationMetacode<I> input) {
                return input.getImplementation();
            }
        });
    }

    public boolean hasImplementation() {
        return !metacodes.isEmpty();
    }
}
