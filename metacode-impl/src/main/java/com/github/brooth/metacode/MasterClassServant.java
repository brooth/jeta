package com.github.brooth.metacode;

import com.github.brooth.metacode.metasitory.Criteria;
import com.github.brooth.metacode.metasitory.Metasitory;

import java.util.List;

/**
 *
 * @param <M>
 * @param <C>
 */
public abstract class MasterClassServant<M, C> {
    protected Class<? extends M> masterClass;
    protected List<C> metacodes;

    @SuppressWarnings("unchecked")
    protected MasterClassServant(Metasitory metasitory, Class<? extends M> masterClass) {
        this.masterClass = masterClass;
        this.metacodes = (List<C>) metasitory.search(criteria());
    }

    protected Criteria criteria() {
        return new Criteria.Builder().masterEqDeep(masterClass).build();
    }
}

