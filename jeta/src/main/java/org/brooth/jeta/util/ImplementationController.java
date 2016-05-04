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

package org.brooth.jeta.util;

import org.brooth.jeta.IMetacode;
import org.brooth.jeta.metasitory.Criteria;
import org.brooth.jeta.metasitory.Metasitory;

import java.util.*;

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
        assert metasitory != null;
        assert of != null;

        Collection<IMetacode<?>> allImplementers =
                metasitory.search(new Criteria.Builder().usesAny(Implementation.class).build());

        metacodes = new ArrayList<>(allImplementers.size());
        for (IMetacode<?> iMetacode : allImplementers) {
            @SuppressWarnings("unchecked")
            ImplementationMetacode<I> metacode = (ImplementationMetacode<I>) iMetacode;
            if (metacode.getImplementationOf() == of)
                metacodes.add(metacode);
        }

        Collections.sort((List<ImplementationMetacode<I>>) metacodes,
                new Comparator<ImplementationMetacode<I>>() {
                    @Override
                    public int compare(ImplementationMetacode<I> o1, ImplementationMetacode<I> o2) {
                        return o1.getImplementationPriority() == o2.getImplementationPriority() ? 0 :
                                o1.getImplementationPriority() > o2.getImplementationPriority() ? -1 : 1;
                    }
                });
    }

    public I getImplementation() {
        if (metacodes.isEmpty())
            return null;

        ImplementationMetacode<I> first = ((List<ImplementationMetacode<I>>) metacodes).get(0);
        if (metacodes.size() > 1 && first.getImplementationPriority() ==
                ((List<ImplementationMetacode<I>>) metacodes).get(1).getImplementationPriority())
            throw new IllegalStateException("More that one implementation with highest priority " + first.getImplementationPriority());

        return first.getImplementation();
    }

    public Collection<I> getImplementations() {
        List<I> result = new ArrayList<>(metacodes.size());
        for (ImplementationMetacode<I> metacode : metacodes)
            result.add(metacode.getImplementation());
        return result;
    }

    public boolean hasImplementation() {
        return !metacodes.isEmpty();
    }
}
