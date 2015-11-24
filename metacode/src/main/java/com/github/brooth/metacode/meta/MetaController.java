package com.github.brooth.metacode.meta;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 *
 */
public class MetaController<M> extends MasterController<M, MetaMetacode<M>> {

    protected MetaController(Metasitory metasitory, M master) {
        super(metasitory, master, Meta.class);
    }

    public void applyMeta(MetaEntityFactory factory) {
        for (MetaMetacode<M> metacode : metacodes)
            metacode.applyMeta(master, factory);
    }
}
