package com.github.brooth.metacode.log;

import com.github.brooth.metacode.MasterMetacode;

import com.github.brooth.metacode.util.Provider;

/**
 * 
 */
public interface LogMetacode extends MasterMetacode<Object> {
    public void apply(Object master, Provider<?> loggerProvider);
}
