package org.javameta.observer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Not thread-safe
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ObserverHandler {

    private static class Record {
        Class row;
        Class column;
        Observers.Handler handler;

        Record(Class row, Class column, Observers.Handler handler){
            this.row = row;
            this.column = column;
            this.handler = handler;
        }
    }

    protected final List<Record> handlers = new ArrayList<>();

    /**
     * used by metacode 
    */
    public void add(Class observableClass, Class eventClass, Observers.Handler handler) {
        handlers.add(new Record(observableClass, eventClass, handler));
    }

    /**
     * add another event handler. it is useful if needed to unregister from all events at the time
     *
     * @param other @ObserverHandler of another event/observable
     */
    public void add(ObserverHandler other) {
        handlers.addAll(other.handlers);
    }

    /**
     * unregister from concrete event of concrete observable
     *
     * @param eventClass      event class unregister from
     * @param observableClass observable class
     * @return number of unregistered events
     */
    public int unregister(Class eventClass, Class observableClass) {
        int result = 0;
        Iterator<Record> records = handlers.iterator();
        while(records.hasNext()) {
            Record record = records.next();
            if(record.row == observableClass && record.column == eventClass) {
                record.handler.unregister();
                records.remove();
                result++;
            }
        }

        return result;
    }

    /**
     * unregister from an event (all events if it is fired by many observables)
     *
     * @param eventClass event class unregister from
     * @return number of unregistered events
     */
    public int unregister(Class eventClass) {
        int result = 0;
        Iterator<Record> records = handlers.iterator();
        while(records.hasNext()) {
            Record record = records.next();
            if(record.column == eventClass) {
                record.handler.unregister();
                records.remove();
                result++;
            }
        }

        return result;
    }

    /**
     * unregister from all the event of a given observable
     *
     * @param observableClass observable class to unregister all event from
     * @return number of unregistered events
     */
    public int unregisterAll(Class observableClass) {
        int result = 0;
        Iterator<Record> records = handlers.iterator();
        while(records.hasNext()) {
            Record record = records.next();
            if(record.row == observableClass) {
                record.handler.unregister();
                records.remove();
                result++;
            }
        }

        return result;
    }

    /**
     * unregister from all the events of all observables
     * @return number of unregistered events
     */
    public int unregisterAll() {
        for (Record record : handlers)
            record.handler.unregister();

        int result = handlers.size();
        handlers.clear();
        return result;
    }
}
