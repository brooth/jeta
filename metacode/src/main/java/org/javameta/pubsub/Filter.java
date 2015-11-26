package org.javameta.pubsub;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Filter {
    boolean accepts(Object master, String methodName, Message msg);
}
