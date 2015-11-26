package org.javameta.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @param <E>
 *
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

    public Handler<E> register(EventObserver<E> observer) {
        observers.add(observer);
        return new Handler<>(observers, observer);
    }

    public boolean unregister(EventObserver<E> observer) {
        return observers.remove(observer);
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
