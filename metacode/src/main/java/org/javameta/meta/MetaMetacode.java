package org.javameta.meta;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface MetaMetacode<M> extends MasterMetacode<M> {
    public void applyMeta(M master, MetaEntityFactory factory);
}
