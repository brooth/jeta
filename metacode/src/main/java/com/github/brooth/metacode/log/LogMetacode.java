package com.github.brooth.metacode.log;

import com.github.brooth.metacode.MasterMetacode;

import javax.inject.Provider;

/**
 * 
 */
public interface LogMetacode extends MasterMetacode<Object> {
    public void apply(Object master, Provider<?> loggerProvider);
}
