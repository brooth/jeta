package org.javameta.meta;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
