package com.github.brooth.metacode.proxy;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ProxyMetaController extends MasterServant<Object, ProxyMetaController.ProxyMetacode<Object>> {

    public ProxyMetaController(Metasitory metasitory, Object master) {
        super(metasitory, master, Proxy.class);
    }

    public void createProxy(Object real) {
        for (ProxyMetacode<Object> metacode : metacodes)
            metacode.applyProxy(master, real);
    }

    public interface ProxyMetacode<M> extends MasterMetacode<M> {
        void applyProxy(M master, Object real);
    }
}
