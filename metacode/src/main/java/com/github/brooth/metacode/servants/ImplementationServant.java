package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.Criteria;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author khalidov
 * @version $Id$
 */
public class ImplementationServant<I> {

    protected List<MasterMetacode> metacodes;
    protected Class<I> of;

    public ImplementationServant(Metasitory metasitory, Class<I> of) {
        this.of = of;
        searchMetacodes(metasitory);
    }

    @SuppressWarnings("unchecked")
    protected void searchMetacodes(Metasitory metasitory) {
        this.metacodes = metasitory.search(new Criteria.Builder().masterEq(of).build());
    }

    @Nullable
    public I getImplementation(Class<I> of) {
        MasterMetacode first = Iterables.getFirst(metacodes, null);
        return first == null ? null : ((ImplementationMetacode) first).getImplementation(of);
    }

    public interface ImplementationMetacode extends MasterMetacode {
        <I> I getImplementation(Class<I> of);
    }
}
