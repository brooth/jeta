package com.github.brooth.metacode.pubsub;

/**
 *
 */
public interface Filter extends IFilter {
    boolean accepts(Object master, String methodName, Message msg);
}
