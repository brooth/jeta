package com.github.brooth.metacode.samples;

import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.observer.ObservableController;
import com.github.brooth.metacode.observer.ObserverController;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.proxy.ProxyController;
import com.github.brooth.metacode.pubsub.PublisherController;
import com.github.brooth.metacode.pubsub.SubscriberController;
import com.github.brooth.metacode.pubsub.SubscriptionHandler;
import com.github.brooth.metacode.util.ImplementationController;
import com.github.brooth.metacode.collector.*;
import com.github.brooth.metacode.validate.ValidationController;
import com.github.brooth.metacode.validate.ValidationException;

import java.lang.annotation.Annotation;
import com.github.brooth.metacode.util.Provider;
import java.util.List;

/**
 * No reflection, no problems...
 * org.metacode?
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

    public static <M> void createPublisher(M master) {
        new PublisherController<>(getInstance().metasitory, master).createPublisher();
    }

    public static <M> SubscriptionHandler registerSubscriber(M master) {
        return new SubscriberController<>(getInstance().metasitory, master).registerSubscriber();
    }

    public static <M> void createObservable(M master) {
        new ObservableController<>(getInstance().metasitory, master).createObservable();
    }

    public static <M> ObserverHandler registerObserver(M observer, Object observable) {
        return new ObserverController<>(getInstance().metasitory, observer).registerObserver(observable);
    }

    public static void validate(Object master) throws ValidationException {
        new ValidationController(getInstance().metasitory, master).validate();
    }

    public static List<String> validateSafe(Object master) {
        return new ValidationController(getInstance().metasitory, master).validateSafe();
    }

    public static void createProxy(Object master, Object real) {
        new ProxyController(getInstance().metasitory, master).createProxy(real);
    }

    public static List<Class> collectTypes(Class masterClass, Class<? extends Annotation> annotationClass) {
        return new TypeCollectorController(getInstance().metasitory, masterClass).getTypes(annotationClass);
    }

    public static List<Provider<?>> collectObjects(Class masterClass, Class<? extends Annotation> annotationClass) {
        return new ObjectCollectorController(getInstance().metasitory, masterClass).getObjects(annotationClass);
    }
}
