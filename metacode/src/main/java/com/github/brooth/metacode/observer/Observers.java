package com.github.brooth.metacode.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @param <E>
 */
public final class Observers<E> {

    private final List<EventObserver<E>> observers =
            Collections.synchronizedList(new ArrayList<EventObserver<E>>());

    public int notify(E event) {
        synchronized (observers) {
            for (EventObserver<E> observer : observers)
                observer.onEvent(event);
            return observers.size();
        }
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

    public Handler<E> register(EventObserver<E> observer) {
        observers.add(observer);
        return new Handler<>(observers, observer);
    }

    public boolean unregister(EventObserver<E> observer) {
        return observers.remove(observer);
    }

    /**
     *
     * @param <E>
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

    /**
     *
     * @param <E>
     */
    public interface EventObserver<E> {
        void onEvent(E event);
    }
}
