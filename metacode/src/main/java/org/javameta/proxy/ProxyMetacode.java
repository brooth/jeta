package org.javameta.proxy;

import org.javameta.MasterMetacode;

/**
 * 
 */
public interface ProxyMetacode<M> extends MasterMetacode<M> {
    void applyProxy(M master, Object real);
}

