package com.github.brooth.metacode.observer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Iterator;
import java.util.Map;

/**
 * Not thread-safe
 */
public final class ObserverHandler {

    private final Table<Class, Class, Observers.Handler> handlers = HashBasedTable.create();

    /**
     * used by metacode to add @see ObserverHandler record
     */
    public void add(Class observableClass, Class eventClass, Observers.Handler handler) {
        handlers.put(observableClass, eventClass, handler);
    }

    /**
     * add another event handler. it is useful if needed to unregister from all events at the time
     *
     * @param other @ObserverHandler of another event/observable
     */
    public void add(ObserverHandler other) {
        handlers.putAll(other.handlers);
    }

    /**
     * unregister from concrete event of concrete observable
     *
     * @param eventClass      event class unregister from
     * @param observableClass observable class
     * @return true if unregistered
     */
    public boolean unregister(Class eventClass, Class observableClass) {
        Observers.Handler handler = handlers.get(observableClass, eventClass);
        if (handler != null) {
            handlers.remove(observableClass, eventClass);
            return handler.unregister();
        }

        return false;
    }

    /**
     * unregister from an event (all events if it is fired by many observables)
     *
     * @param eventClass event class unregister from
     * @return true if there is at least one observable for given event
     */
    public boolean unregister(Class eventClass) {
        Map<Class, Observers.Handler> column = handlers.column(eventClass);

        if (column != null) {
            Iterator<Class> events = column.keySet().iterator();
            while (events.hasNext()) {
                Class event = events.next();
                column.get(event).unregister();
                events.remove();
            }
        }

        return false;
    }

    /**
     * unregister from all the event of a given observable
     *
     * @param observableClass observable class to unregister all event from
     * @return true if there is at least one event for given observable
     */
    public boolean unregisterAll(Class observableClass) {
        Map<Class, Observers.Handler> row = handlers.row(observableClass);
        if (row != null) {
            Iterator<Class> observables = row.keySet().iterator();
            while (observables.hasNext()) {
                Class observable = observables.next();
                row.get(observable).unregister();
                observables.remove();
            }

            return true;
        }

        return false;
    }

    /**
     * unregister from all the events of all observables
     */
    public void unregisterAll() {
        for (Observers.Handler handler : handlers.values())
            handler.unregister();

        handlers.clear();
    }
}
