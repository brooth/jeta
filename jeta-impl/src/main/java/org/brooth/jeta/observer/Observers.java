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

package org.brooth.jeta.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @param <E>
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class Observers<E> {

    private final List<EventObserver<E>> observers = new CopyOnWriteArrayList<>();

    public int notify(E event) {
        int result = observers.size();
        for (EventObserver<E> observer : observers)
            observer.onEvent(event);
        return result;
    }

    public int notifyAndClear(E event) {
        int result = notify(event);
        clear();
        return result;
    }

    public void clear() {
        observers.clear();
    }

    public List<EventObserver<E>> getAll() {
        return observers;
    }

    protected void addAll(List<EventObserver<E>> list) {
        observers.addAll(list);
    }

    public Handler<E> register(EventObserver<E> observer) {
        observers.add(observer);
        return new Handler<>(observers, observer);
    }

    /**
     * @param <E> event type
     */
    public static final class Handler<E> {
        private List<EventObserver<E>> observers;
        private EventObserver<E> observer;

        private Handler(List<EventObserver<E>> observers, EventObserver<E> observer) {
            this.observers = observers;
            this.observer = observer;
        }

        public boolean unregister() {
            return observers.remove(observer);
        }
    }
}
