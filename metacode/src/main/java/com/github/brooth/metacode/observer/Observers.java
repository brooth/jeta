package com.github.brooth.metacode.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @param <E>
 */
public final class Observers<E> {

    private final List<Observer<E>> observers =
            Collections.synchronizedList(new ArrayList<Observer<E>>());

    public int notify(E event) {
        synchronized (observers) {
            for (Observer<E> observer : observers)
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

    public List<Observer<E>> getAll() {
        return observers;
    }

    public Handler<E> register(Observer<E> observer) {
        observers.add(observer);
        return new Handler<>(observers, observer);
    }

    public boolean unregister(Observer<E> observer) {
        return observers.remove(observer);
    }

    /**
     *
     * @param <E>
     */
    public static final class Handler<E> {
        private List<Observer<E>> observers;
        private Observer<E> observer;

        private Handler(List<Observer<E>> observers, Observer<E> observer) {
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
    public interface Observer<E> {
        void onEvent(E event);
    }
}
