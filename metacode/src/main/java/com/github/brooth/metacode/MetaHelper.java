package com.github.brooth.metacode;

import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.util.ImplementationServant;
import com.github.brooth.metacode.log.LogServant;

import javax.inject.Provider;

/*
 * todo move to samples
 */
public class MetaHelper {

    private static MetaHelper instance;

    private final Metasitory metasitory;

    private final Provider<?> logProvider;

    private MetaHelper(String metaPackage, Provider<?> logProvider) {
        metasitory = HashMapMetasitory.getInstance(metaPackage);
        this.logProvider = logProvider;
    }

    public static MetaHelper init(String metaPackage, Provider<?> logProvider) {
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
        return getImplementationServant(of).getImplementation();
    }

    // others
}
