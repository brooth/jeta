/*
 * Copyright 2016 Oleg Khalidov
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

import org.brooth.jeta.observer.EventObserver;
import org.brooth.jeta.observer.Observers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class Subscribers<E extends Message> {

    private Observers<E> observers = new ObserversDecorator<E>();

    public int notify(E event) {
        return observers.notify(event);
    }

    public int notifyAndClear(E event) {
        return observers.notifyAndClear(event);
    }

    public void clear() {
        observers.clear();
    }

    public List<EventObserver<E>> getAll() {
        return observers.getAll();
    }

    public synchronized Observers.Handler<E> register(EventObserver<E> observer, int priority) {
        Observers.Handler<E> handler = observers.register(new PriorityEventObserver<E>(observer, priority));
        ((ObserversDecorator<E>) observers).order();
        return handler;
    }

    private static class PriorityEventObserver<E> implements EventObserver<E> {
        EventObserver<E> observer;
        int priority;

        private PriorityEventObserver(EventObserver<E> observer, int priority) {
            this.observer = observer;
            this.priority = priority;
        }

        public void onEvent(E event) {
            observer.onEvent(event);
        }
    }

    private static class ObserversDecorator<E> extends Observers<E> {
        private void order() {
            List<EventObserver<E>> copy = new ArrayList<EventObserver<E>>(getAll());
            Collections.sort(copy, new PriorityComparator());
            clear();
            addAll(copy);
        }
    }

    private static class PriorityComparator implements Comparator<EventObserver<?>> {
        public int compare(EventObserver<?> o1, EventObserver<?> o2) {
            return ((PriorityEventObserver<?>) o1).priority ==
                    ((PriorityEventObserver<?>) o2).priority ? 0 :
                    ((PriorityEventObserver<?>) o1).priority >
                            ((PriorityEventObserver<?>) o2).priority ? -1 : 1;
        }
    }
}
