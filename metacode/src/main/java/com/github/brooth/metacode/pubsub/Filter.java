package com.github.brooth.metacode.pubsub;

/**
 *
 */
public interface Filter {
    boolean accepts(Object master, String methodName, Message msg);
}
