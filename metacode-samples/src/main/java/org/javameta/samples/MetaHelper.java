package org.javameta.samples;

import org.javameta.collector.ObjectCollectorController;
import org.javameta.collector.TypeCollectorController;
import org.javameta.metasitory.HashMapMetasitory;
import org.javameta.metasitory.Metasitory;
import org.javameta.observer.ObservableController;
import org.javameta.observer.ObserverController;
import org.javameta.observer.ObserverHandler;
import org.javameta.proxy.ProxyController;
import org.javameta.pubsub.PublisherController;
import org.javameta.pubsub.SubscriberController;
import org.javameta.pubsub.SubscriptionHandler;
import org.javameta.util.ImplementationController;
import org.javameta.util.Provider;
import org.javameta.validate.ValidationController;
import org.javameta.validate.ValidationException;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * No reflection, no problems...
 * Feel the power of javax.annotation.processing
 */
public class MetaHelper {

    private static MetaHelper instance;

    private final Metasitory metasitory;

    public static MetaHelper getInstance() {
        if (instance == null)
            instance = new MetaHelper("org.javameta.samples");
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
