package org.javameta.log;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface NamedLoggerProvider<T> {
    T get(String name);
}
