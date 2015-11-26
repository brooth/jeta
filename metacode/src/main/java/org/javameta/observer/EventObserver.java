package org.javameta.observer;

/**
 * @param <E>
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface EventObserver<E> {
    void onEvent(E event);
}
