package com.github.brooth.metacode.meta;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 *
 */
public class MetaServant extends MasterServant<Object, MetaServant.MetaMetacode> {

    protected final MetaEntityFactory factory;

    protected MetaServant(Metasitory metasitory, MetaEntityFactory factory, Object master) {
        super(metasitory, master, Meta.class);
        this.factory = factory;
    }

    public void applyMeta() {
        for (MetaMetacode metacode : metacodes)
            metacode.applyMeta(master, factory);
    }

    public interface MetaMetacode extends MasterMetacode<Object> {
        public void applyMeta(Object master, MetaEntityFactory factory);
    }
}
