package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.Criteria;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 */
public class ImplementationController<I> {

    protected List<MasterMetacode> metacodes;
    protected Class<I> of;

    public ImplementationController(Metasitory metasitory, Class<I> of) {
        this.of = of;
        searchMetacodes(metasitory);
    }

    @SuppressWarnings("unchecked")
    protected void searchMetacodes(Metasitory metasitory) {
        this.metacodes = metasitory.search(new Criteria.Builder().masterEq(of).build());
    }

    @Nullable
    public I getImplementation() {
        MasterMetacode first = Iterables.getFirst(metacodes, null);
        return first == null ? null : ((ImplementationMetacode) first).getImplementation(of);
    }

    public List<I> getImplementations() {
        return Lists.transform(metacodes, new Function<MasterMetacode, I>() {
            @Override
            public I apply(MasterMetacode input) {
                return ((ImplementationMetacode) input).getImplementation(of);
            }
        });
    }

    public interface ImplementationMetacode extends MasterMetacode {
        <I> I getImplementation(Class<I> of);
    }
}
