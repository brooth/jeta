package org.javameta;

import org.javameta.base.MetaController;
import org.javameta.base.MetaEntityFactory;
import org.javameta.collector.ObjectCollectorController;
import org.javameta.collector.TypeCollectorController;
import org.javameta.log.LogController;
import org.javameta.log.NamedLoggerProvider;
import org.javameta.metasitory.MapMetasitory;
import org.javameta.metasitory.Metasitory;
import org.javameta.observer.ObservableController;
import org.javameta.observer.ObserverController;
import org.javameta.observer.ObserverHandler;
import org.javameta.proxy.ProxyController;
import org.javameta.pubsub.PublisherController;
import org.javameta.pubsub.SubscriberController;
import org.javameta.pubsub.SubscriptionHandler;
import org.javameta.util.ImplementationController;
import org.javameta.util.MultitonController;
import org.javameta.util.Provider;
import org.javameta.util.SingletonController;
import org.javameta.validate.ValidationController;
import org.javameta.validate.ValidationException;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class TestMetaHelper {

    private static TestMetaHelper instance;

    private final Metasitory metasitory;

    private final MetaEntityFactory metaEntityFactory;

    private NamedLoggerProvider<Logger> loggerProvider;

    public static TestMetaHelper getInstance() {
        if (instance == null)
            instance = new TestMetaHelper("org.javameta");
        return instance;
    }

    private TestMetaHelper(String metaPackage) {
        metasitory = new MapMetasitory(metaPackage);
        metaEntityFactory = new MetaEntityFactory(metasitory);

        loggerProvider = new NamedLoggerProvider<Logger>() {
            @Override
            public Logger get(String name) {
                Logger logger = Logger.getLogger(name);
                logger.setLevel(Level.FINE);
                return logger;
            }
        };
    }

    public static void injectMeta(Object master) {
        new MetaController(getInstance().metasitory, master).injectMeta(getInstance().metaEntityFactory);
    }

    public static <I> ImplementationController<I> getImplementationController(Class<I> of) {
        return new ImplementationController<>(getInstance().metasitory, of);
    }

    public static <I> I getImplementation(Class<I> of) {
        return getImplementationController(of).getImplementation();
    }

    public static void createPublisher(Object master) {
        new PublisherController<>(getInstance().metasitory, master).createPublisher();
    }

    public static SubscriptionHandler registerSubscriber(Object master) {
        return new SubscriberController<>(getInstance().metasitory, master).registerSubscriber();
    }

    public static void createObservable(Object master) {
        new ObservableController<>(getInstance().metasitory, master).createObservable();
    }

    public static ObserverHandler registerObserver(Object observer, Object observable) {
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

    public static void createLogger(Object master) {
        new LogController(getInstance().metasitory, master).createLogger(getInstance().loggerProvider);
    }

    public static void createSingleton(Class<?> masterClass) {
        new SingletonController(getInstance().metasitory, masterClass).createSingleton();
    }

    public static void createMultitonInstance(Class<?> masterClass, Object key) {
        new MultitonController<>(getInstance().metasitory, masterClass).createInstance(key);
    }
}
