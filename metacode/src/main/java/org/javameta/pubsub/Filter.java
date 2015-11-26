package org.javameta.pubsub;

/**
 *
 */
public interface Filter {
    boolean accepts(Object master, String methodName, Message msg);
}
