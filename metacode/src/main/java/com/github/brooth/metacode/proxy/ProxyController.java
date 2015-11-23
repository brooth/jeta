package com.github.brooth.metacode.proxy;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ProxyController extends MasterController<Object, ProxyMetacode<Object>> {

    public ProxyController(Metasitory metasitory, Object master) {
        super(metasitory, master, Proxy.class);
    }

    public void createProxy(Object real) {
        for (ProxyMetacode<Object> metacode : metacodes)
            metacode.applyProxy(master, real);
    }
}
