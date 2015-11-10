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
public class MetaHelper {

    private static MetaHelper instance;

    private final Metasitory metasitory;

    private final Provider<NamedLogger> logProvider;

    private MetaHelper(String metaPackage, Provider<NamedLogger> logProvider) {
        metasitory = HashMapMetasitory.getInstance(metaPackage);
        this.logProvider = logProvider;
    }

    public static MetaHelper init(String metaPackage, Provider<NamedLogger> logProvider) {
        instance = new MetaHelper(metaPackage, logProvider);
        return instance;
    }

    public static MetaHelper getInstance() {
        if (instance == null)
            throw new IllegalStateException("Not initialized");

        return instance;
    }

    public void applyLogs(Object master) {
        new LogServant(metasitory, master).apply(logProvider);
    }

    public <I> ImplementationServant<I> getImplementationServant(Class<I> of) {
        return new ImplementationServant<>(metasitory, of);
    }

    public <I> I getImplementation(Class<I> of) {
        return getImplementationServant(of).getImplementation(of);
    }

    // others
}
