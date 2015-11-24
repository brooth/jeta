package com.github.brooth.metacode.proxy;

import com.github.brooth.metacode.MasterMetacode;

/**
 * @author khalidov
 * @version $Id$
 */
public interface ProxyMetacode<M> extends MasterMetacode<M> {
    void applyProxy(M master, Object real);
}

