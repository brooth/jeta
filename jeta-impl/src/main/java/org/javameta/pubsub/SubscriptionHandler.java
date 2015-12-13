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

package org.javameta.pubsub;

import org.javameta.observer.ObserverHandler;
import org.javameta.observer.Observers;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SubscriptionHandler {

    private final ObserverHandler handler = new ObserverHandler();

    public void add(Class<?> publisherClass, Class<?> eventClass, Observers.Handler<?> handler) {
        this.handler.add(publisherClass, eventClass, handler);
    }

    public void add(SubscriptionHandler other) {
        handler.add(other.handler);
    }

    public int unregister(Class<?> eventClass, Class<?> publisherClass) {
        return handler.unregister(eventClass, publisherClass);
    }

    public int unregister(Class<?> eventClass) {
        return handler.unregister(eventClass);
    }

    public int unregisterAll(Class<?> publisherClass) {
        return handler.unregisterAll(publisherClass);
    }

    public int unregisterAll() {
        return handler.unregisterAll();
    }
}
