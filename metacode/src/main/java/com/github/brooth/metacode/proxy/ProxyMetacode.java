package com.github.brooth.metacode.proxy;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public interface ProxyMetacode<M> extends MasterMetacode<M> {
    void applyProxy(M master, Object real);
}

