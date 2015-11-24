package com.github.brooth.metacode.proxy;

import com.github.brooth.metacode.MasterMetacode;

/**
 * 
 */
public interface ProxyMetacode<M> extends MasterMetacode<M> {
    void applyProxy(M master, Object real);
}

