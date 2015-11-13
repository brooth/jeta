package com.github.brooth.metacode.samples;

import com.github.brooth.metacode.broadcast.Message;
import com.github.brooth.metacode.broadcast.ReceiverHandler;
import com.github.brooth.metacode.log.LogServant;
import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.util.ImplementationServant;

import javax.inject.Provider;

/*
 *
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

    public static void broadcastMessage(Class<?> masterClass, Message msg) {

    }

    public static ReceiverHandler registerReceiver(Object receiver, Class<?> broadcasterClass) {
        return null;
    }

    public static void createObservable(Object observer) {

    }

    public static ObserverHandler registerObserver(Object observer, Object observable) {

        return null;
    }
}
