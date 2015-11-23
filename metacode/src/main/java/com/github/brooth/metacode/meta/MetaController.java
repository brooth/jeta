package com.github.brooth.metacode.meta;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 *
 */
public class MetaController<M> extends MasterController<M, MetaMetacode<M>> {

    protected final MetaEntityFactory factory;

    protected MetaController(Metasitory metasitory, MetaEntityFactory factory, M master) {
        super(metasitory, master, Meta.class);
        this.factory = factory;
    }

    public void applyMeta() {
        for (MetaMetacode<M> metacode : metacodes)
            metacode.applyMeta(master, factory);
    }
}
