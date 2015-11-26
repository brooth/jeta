package org.javameta.meta;

import org.javameta.MasterMetacode;

/**
 *
 */
public interface MetaMetacode<M> extends MasterMetacode<M> {
    public void applyMeta(M master, MetaEntityFactory factory);
}
