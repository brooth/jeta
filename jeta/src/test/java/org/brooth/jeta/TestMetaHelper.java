/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta;

import org.brooth.jeta.meta.MetaController;
import org.brooth.jeta.meta.MetaEntityFactory;
import org.brooth.jeta.collector.ObjectCollectorController;
import org.brooth.jeta.collector.TypeCollectorController;
import org.brooth.jeta.log.LogController;
import org.brooth.jeta.log.NamedLoggerProvider;
import org.brooth.jeta.metasitory.MapMetasitory;
import org.brooth.jeta.metasitory.Metasitory;
import org.brooth.jeta.observer.ObservableController;
import org.brooth.jeta.observer.ObserverController;
import org.brooth.jeta.observer.ObserverHandler;
import org.brooth.jeta.proxy.ProxyController;
import org.brooth.jeta.pubsub.PublisherController;
import org.brooth.jeta.pubsub.SubscriberController;
import org.brooth.jeta.pubsub.SubscriptionHandler;
import org.brooth.jeta.util.ImplementationController;
import org.brooth.jeta.util.MultitonController;
import org.brooth.jeta.util.MultitonMetacode;
import org.brooth.jeta.util.Provider;
import org.brooth.jeta.util.SingletonController;
import org.brooth.jeta.util.SingletonMetacode;
import org.brooth.jeta.validate.ValidationController;
import org.brooth.jeta.validate.ValidationException;

import java.lang.annotation.Annotation;
import java.util.List;

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
            instance = new TestMetaHelper("org.brooth.jeta.tests");
        return instance;
    }

    private TestMetaHelper(String metaPackage) {
        metasitory = new MapMetasitory(metaPackage);
        metaEntityFactory = new MetaEntityFactory(metasitory);

        loggerProvider = new NamedLoggerProvider<Logger>() {
            @Override
            public Logger get(String name) {
                return new Logger(name);
            }
        };
    }

    public static void injectMeta(Object master) {
        new MetaController(getInstance().metasitory, master).injectMeta(getInstance().metaEntityFactory);
    }

    public static <I> ImplementationController<I> implementationController(Class<I> of) {
        return new ImplementationController<>(getInstance().metasitory, of);
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

    public static ValidationController validationController(Object master) throws ValidationException {
        return new ValidationController(getInstance().metasitory, master);
    }

    public static void validate(Object master) throws ValidationException {
        validationController(master).validate();
    }

    public static void createProxy(Object master, Object real) {
        new ProxyController(getInstance().metasitory, master).createProxy(real);
    }

    public static List<Class<?>>  collectTypes(Class<?> masterClass, Class<? extends Annotation> annotationClass) {
        return new TypeCollectorController(getInstance().metasitory, masterClass).getTypes(annotationClass);
    }

    public static List<Provider<?>> collectObjects(Class<?> masterClass, Class<? extends Annotation> annotationClass) {
        return new ObjectCollectorController(getInstance().metasitory, masterClass).getObjects(annotationClass);
    }

    public static void createLogger(Object master) {
        new LogController(getInstance().metasitory, master).createLogger(getInstance().loggerProvider);
    }

    public static <M> SingletonMetacode<M> getSingleton(Class<M> masterClass) {
        return new SingletonController<>(getInstance().metasitory, masterClass).getMetacode();
    }

    public static <M> MultitonMetacode<M> getMultiton(Class<M> masterClass) {
        return new MultitonController<>(getInstance().metasitory, masterClass).getMetacode();
    }
}
