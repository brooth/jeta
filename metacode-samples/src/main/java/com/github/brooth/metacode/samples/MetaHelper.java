package com.github.brooth.metacode.samples;

import com.github.brooth.metacode.pubsub.Message;
import com.github.brooth.metacode.pubsub.SubscriptionHandler;
import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.samples.validate.ValidatorSample;
import com.github.brooth.metacode.util.ImplementationServant;
import com.github.brooth.metacode.validate.ValidationServant;

/**
 *
 */
public class MetaHelper {

    private static MetaHelper instance;

    private final Metasitory metasitory;

    private MetaHelper(String metaPackage) {
        metasitory = HashMapMetasitory.getInstance(metaPackage);
    }

    public static MetaHelper init(String metaPackage) {
        instance = new MetaHelper(metaPackage);
        return instance;
    }

    public static MetaHelper getInstance() {
        if (instance == null)
            throw new IllegalStateException("Not initialized");

        return instance;
    }

    public <I> ImplementationServant<I> getImplementationServant(Class<I> of) {
        return new ImplementationServant<>(metasitory, of);
    }

    public <I> I getImplementation(Class<I> of) {
        return getImplementationServant(of).getImplementation();
    }

    public static void publishMessage(Class<?> masterClass, Message msg) {

    }

    public static SubscriptionHandler registerSubscriber(Object subscriber) {
        return null;
    }

    public static void createObservable(Object observer) {

    }

    public static ObserverHandler registerObserver(Object observer, Object observable) {
        return null;
    }

    public static void validate(Object master) {
        new ValidationServant(getInstance().metasitory, master).validate();
    }
}
