package com.github.brooth.metacode.log;

import com.github.brooth.metacode.MasterMetacode;

import javax.inject.Provider;

/**
* @author khalidov
* @version $Id$
*/
public interface LogMetacode extends MasterMetacode<Object> {
    public void apply(Object master, Provider<?> loggerProvider);
}
