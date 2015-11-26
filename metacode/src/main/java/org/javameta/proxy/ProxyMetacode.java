package org.javameta.proxy;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface ProxyMetacode<M> extends MasterMetacode<M> {
    void applyProxy(M master, Object real);
}

