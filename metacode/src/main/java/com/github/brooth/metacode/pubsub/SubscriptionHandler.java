package com.github.brooth.metacode.pubsub;

import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.observer.Observers;

/**
 * @author khalidov
 * @version $Id$
 */
public class SubscriptionHandler {

    private final ObserverHandler handler = new ObserverHandler();

    public void add(Class publisherClass, Class eventClass, Observers.Handler handler) {
        this.handler.add(publisherClass, eventClass, handler);
    }

    public void add(SubscriptionHandler other) {
        handler.add(other.handler);
    }

    public boolean unregister(Class eventClass, Class publisherClass) {
        return handler.unregister(eventClass, publisherClass);
    }

    public boolean unregister(Class eventClass) {
        return handler.unregister(eventClass);
    }

    public boolean unregisterAll(Class publisherClass) {
        return handler.unregisterAll(publisherClass);
    }

    public void unregisterAll() {
        handler.unregisterAll();
    }
}
