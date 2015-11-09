package com.github.brooth.metacode;

import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.servants.ImplementationServant;
import com.github.brooth.metacode.servants.LogServant;

/*
 * todo move to samples
 */
public class Metacode {

    private Metasitory metasitory;

    public Metacode(String metaPackage) {
        metasitory = HashMapMetasitory.getInstance(metaPackage);
    }

    public void applyLogs(Object master) {
        new LogServant(metasitory, master).apply();
    }

    public <M> ImplementationServant<M> getImpl(Class<? extends M> master) {
        return new ImplementationServant<>(metasitory, master);
    }

    // others
}
