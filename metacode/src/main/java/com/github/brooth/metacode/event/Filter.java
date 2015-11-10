package com.github.brooth.metacode.event;

/**
 * 
 */
public interface Filter {
    boolean filter(Object master, String handlerName, Object event);
}
