package com.github.brooth.metacode.samples;

import com.github.brooth.metacode.observer.ObservableController;
import com.github.brooth.metacode.observer.ObserverController;
import com.github.brooth.metacode.proxy.ProxyController;
import com.github.brooth.metacode.pubsub.Message;
import com.github.brooth.metacode.pubsub.SubscriptionHandler;
import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.util.ImplementationController;
import com.github.brooth.metacode.validate.ValidationController;

/**
 *
 */
public class MetaHelper {

    private static MetaHelper instance;

    private final Metasitory metasitory;

    public static MetaHelper getInstance() {
        if (instance == null)
            instance = new MetaHelper("com.github.brooth.metacode.samples");
        return instance;
    }

    private MetaHelper(String metaPackage) {
        metasitory = new HashMapMetasitory(metaPackage);
    }

    public <I> ImplementationController<I> getImplementationController(Class<I> of) {
        return new ImplementationController<>(metasitory, of);
    }

    public <I> I getImplementation(Class<I> of) {
        return getImplementationController(of).getImplementation();
    }

    public static void publishMessage(Class<?> masterClass, Message msg) {

    }

    public static SubscriptionHandler registerSubscriber(Object subscriber) {
        return null;
    }

    public static <M> void createObservable(M observer) {
        new ObservableController<>(getInstance().metasitory, observer).createObservable();
    }

    public static <M> ObserverHandler registerObserver(M observer, Object observable) {
        return new ObserverController<>(getInstance().metasitory, observer).registerObserver(observable);
    }

    public static void validate(Object master) {
        new ValidationController(getInstance().metasitory, master).validate();
    }

    public static void createProxy(Object master, Object real) {
        new ProxyController(getInstance().metasitory, master).createProxy(real);
    }
}
