package org.javameta.observer;

/**
 *
 * @param <E>
 */
public interface EventObserver<E> {
    void onEvent(E event);
}
