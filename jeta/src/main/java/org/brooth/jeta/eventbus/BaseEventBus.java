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

package org.brooth.jeta.eventbus;

import com.google.common.base.Preconditions;
import org.brooth.jeta.observer.EventObserver;
import org.brooth.jeta.observer.Observers;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class BaseEventBus implements EventBus {

    protected ConcurrentHashMap<Class<?>, Subscribers<?>> subscribersMap = new ConcurrentHashMap<>();

    @Override
    public <E extends Message> Observers.Handler<E> register(Class<E> eventClass, EventObserver<E> observer, int priority) {
        Preconditions.checkNotNull(eventClass, "eventClass");
        Preconditions.checkNotNull(eventClass, "observer");

        @SuppressWarnings("unchecked")
        Subscribers<E> subscribers = (Subscribers<E>) subscribersMap.get(eventClass);
        if (subscribers == null) {
            subscribers = new Subscribers<>();
            @SuppressWarnings("unchecked")
            Subscribers<E> quicker = (Subscribers<E>) subscribersMap.putIfAbsent(eventClass, subscribers);
            if (quicker != null)
                subscribers = quicker;
        }

        return subscribers.register(observer, priority);
    }

    @Override
    public <E extends Message> void publish(E event) {
        Preconditions.checkNotNull(event, "event");

        @SuppressWarnings("unchecked")
        Subscribers<E> subscribers = (Subscribers<E>) subscribersMap.get(event.getClass());
        if (subscribers != null) {
            subscribers.notify(event);
        }
    }
}
