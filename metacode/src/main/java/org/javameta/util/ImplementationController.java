package org.javameta.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.javameta.MasterMetacode;
import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 *
 */
public class ImplementationController<I> {

    protected Collection<MasterMetacode> metacodes;
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

    public Collection<I> getImplementations() {
        return Collections2.transform(metacodes, new Function<MasterMetacode, I>() {
            @Override
            public I apply(MasterMetacode input) {
                return ((ImplementationMetacode) input).getImplementation(of);
            }
        });
    }

}
