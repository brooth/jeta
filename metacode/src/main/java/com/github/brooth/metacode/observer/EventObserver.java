package com.github.brooth.metacode.observer;

/**
 *
 * @param <E>
 */
public interface EventObserver<E> {
    void onEvent(E event);
}
