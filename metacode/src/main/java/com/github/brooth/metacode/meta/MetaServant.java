package com.github.brooth.metacode.meta;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 *
 */
public class MetaServant<M> extends MasterServant<M, MetaServant.MetaMetacode<M>> {

    protected final MetaEntityFactory factory;

    protected MetaServant(Metasitory metasitory, MetaEntityFactory factory, M master) {
        super(metasitory, master, Meta.class);
        this.factory = factory;
    }

    public void applyMeta() {
        for (MetaMetacode<M> metacode : metacodes)
            metacode.applyMeta(master, factory);
    }

    public interface MetaMetacode<M> extends MasterMetacode<M> {
        public void applyMeta(M master, MetaEntityFactory factory);
    }
}
