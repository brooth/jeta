/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javameta.samples;

import org.javameta.collector.ObjectCollectorController;
import org.javameta.collector.TypeCollectorController;
import org.javameta.log.LogController;
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

/**
 * No reflection, no problems...
 * Feel the power of javax.annotation.processing
 *
 * @author Oleg Khalidov (brooth@gmail.com)
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
        metasitory = new MapMetasitory(metaPackage);
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

    public static void createLogger(Object master, Provider<?> provider) {
        new LogController(getInstance().metasitory, master).createLogger(provider);
    }

    public static void createSingleton(Class<?> masterClass) {
        new SingletonController(getInstance().metasitory, masterClass).createSingleton();
    }

    public static void createMultitonInstance(Class<?> masterClass, Object key) {
        new MultitonController<>(getInstance().metasitory, masterClass).createInstance(key);
    }
}
