package com.github.brooth.metacode.meta;

import com.github.brooth.metacode.MasterMetacode;

/**
 *
 */
public interface MetaMetacode<M> extends MasterMetacode<M> {
    public void applyMeta(M master, MetaEntityFactory factory);
}
