package com.github.brooth.metacode.broadcast;

/**
 *
 */
public interface Filter {
    boolean accepts(Object master, String methodName, Message msg);
}
