/*
 * Copyright 2016 Oleg Khalidov
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
 *
 */

package org.brooth.jeta;

import org.brooth.jeta.collector.ObjectCollectorController;
import org.brooth.jeta.collector.TypeCollectorController;
import org.brooth.jeta.eventbus.BaseEventBus;
import org.brooth.jeta.eventbus.EventBus;
import org.brooth.jeta.eventbus.SubscriberController;
import org.brooth.jeta.eventbus.SubscriptionHandler;
import org.brooth.jeta.inject.InjectController;
import org.brooth.jeta.inject.MetaScope;
import org.brooth.jeta.inject.MetaScopeController;
import org.brooth.jeta.inject.StaticInjectController;
import org.brooth.jeta.log.LogController;
import org.brooth.jeta.log.NamedLoggerProvider;
import org.brooth.jeta.metasitory.MapMetasitory;
import org.brooth.jeta.metasitory.Metasitory;
import org.brooth.jeta.observer.ObservableController;
import org.brooth.jeta.observer.ObserverController;
import org.brooth.jeta.observer.ObserverHandler;
import org.brooth.jeta.proxy.ProxyController;
import org.brooth.jeta.tests.inject.DefaultScope;
import org.brooth.jeta.util.*;
import org.brooth.jeta.validate.ValidationController;
import org.brooth.jeta.validate.ValidationException;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaHelper {

    private static MetaHelper instance;

    private final Metasitory metasitory;
    private final BaseEventBus bus;

    private final MetaScope<DefaultScope> defaultScope;

    private NamedLoggerProvider<Logger> loggerProvider;

    public static MetaHelper getInstance() {
        if (instance == null)
            instance = new MetaHelper("org.brooth.jeta.tests");
        return instance;
    }

    private MetaHelper(String metaPackage) {
        metasitory = new MapMetasitory(metaPackage);
        defaultScope = new MetaScopeController<DefaultScope>(metasitory, new DefaultScope()).get();
        bus = new BaseEventBus();
        loggerProvider = new NamedLoggerProvider<Logger>() {
            public Logger get(String name) {
                return new Logger(name);
            }
        };
    }

    public static void injectMeta(Object master) {
        new InjectController(getInstance().metasitory, master, Inject.class).inject(getInstance().defaultScope);
    }

    public static void injectMeta(MetaScope<?> scope, Object master) {
        new InjectController(getInstance().metasitory, master, Inject.class).inject(scope);
    }

    public static void injectStaticMeta(Class<?> masterClass) {
        new StaticInjectController(getInstance().metasitory, masterClass, Inject.class).inject(getInstance().defaultScope);
    }

    public static void injectStaticMeta(MetaScope<?> scope, Class<?> masterClass) {
        new StaticInjectController(getInstance().metasitory, masterClass, Inject.class).inject(scope);
    }

    public static <I> ImplementationController<I> implementationController(Class<I> of) {
        return new ImplementationController<I>(getInstance().metasitory, of);
    }

    public static EventBus getEventBus() {
        return getInstance().bus;
    }

    public static SubscriptionHandler registerSubscriber(Object master) {
        return new SubscriberController<Object>(getInstance().metasitory, master).registerSubscriber(getEventBus());
    }

    public static void createObservable(Object master) {
        new ObservableController<Object>(getInstance().metasitory, master).createObservable();
    }

    public static ObserverHandler registerObserver(Object observer, Object observable) {
        return new ObserverController<Object>(getInstance().metasitory, observer).registerObserver(observable);
    }

    public static ValidationController validationController(Object master) {
        return new ValidationController(getInstance().metasitory, master);
    }

    public static ValidationController validationController(Object master, Set<Class<? extends Annotation>> validators) {
        return new ValidationController(getInstance().metasitory, master, validators);
    }

    public static void validate(Object master) throws ValidationException {
        validationController(master).validate();
    }

    public static void createProxy(Object master, Object real) {
        new ProxyController(getInstance().metasitory, master).createProxy(real);
    }

    public static List<Class<?>> collectTypes(Class<?> masterClass, Class<? extends Annotation> annotationClass) {
        return new TypeCollectorController(getInstance().metasitory, masterClass).getTypes(annotationClass);
    }

    public static List<Provider<?>> collectObjects(Class<?> masterClass, Class<? extends Annotation> annotationClass) {
        return new ObjectCollectorController(getInstance().metasitory, masterClass).getObjects(annotationClass);
    }

    public static void createLogger(Object master) {
        new LogController(getInstance().metasitory, master).createLoggers(getInstance().loggerProvider);
    }

    public static <M> SingletonMetacode<M> getSingleton(Class<M> masterClass) {
        return new SingletonController<M>(getInstance().metasitory, masterClass).getMetacode();
    }

    public static <M> MultitonMetacode<M> getMultiton(Class<M> masterClass) {
        return new MultitonController<M>(getInstance().metasitory, masterClass).getMetacode();
    }

    public static <S> MetaScope<S> getMetaScope(S scope) {
        return new MetaScopeController<S>(getInstance().metasitory, scope).get();
    }
}
