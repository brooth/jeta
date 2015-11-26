package org.javameta.proxy;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;

/**
 * 
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
