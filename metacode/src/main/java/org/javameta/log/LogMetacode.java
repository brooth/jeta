package org.javameta.log;

import org.javameta.util.Provider;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface LogMetacode {
    public void apply(Object master, Provider<?> loggerProvider);
}
