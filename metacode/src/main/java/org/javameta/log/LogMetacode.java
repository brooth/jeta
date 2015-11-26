package org.javameta.log;

import org.javameta.MasterMetacode;
import org.javameta.util.Provider;

/**
 * 
 */
public interface LogMetacode extends MasterMetacode<Object> {
    public void apply(Object master, Provider<?> loggerProvider);
}
