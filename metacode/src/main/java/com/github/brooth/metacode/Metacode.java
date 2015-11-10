package com.github.brooth.metacode;

import com.github.brooth.metacode.log.NamedLogger;
import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.servants.ImplementationServant;
import com.github.brooth.metacode.servants.LogServant;

import javax.inject.Provider;

/*
 * todo move to samples
 */
public class Metacode {

    private Metasitory metasitory;

    public Metacode(String metaPackage) {
        metasitory = HashMapMetasitory.getInstance(metaPackage);
    }

    public void applyLogs(Object master, Provider<NamedLogger> provider) {
        new LogServant(metasitory, master).apply(provider);
    }

    public <I> ImplementationServant<I> getImplementationServant(Class<I> of) {
        return new ImplementationServant<>(metasitory, of);
    }

    public <I> I getImplementation(Class<I> of) {
        return getImplementationServant(of).getImplementation(of);
    }

    // others
}
