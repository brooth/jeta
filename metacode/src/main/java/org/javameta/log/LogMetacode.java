package org.javameta.log;

import org.javameta.util.Provider;

/**
 * 
 */
public interface LogMetacode {
    public void apply(Object master, Provider<?> loggerProvider);
}
