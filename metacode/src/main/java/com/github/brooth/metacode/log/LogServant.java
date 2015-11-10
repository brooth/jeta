package com.github.brooth.metacode.log;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

import javax.inject.Provider;

/**
 *
 */
public class LogServant extends MasterServant<Object, LogServant.LogMetacode> {

    public LogServant(Metasitory metasitory, Object master) {
        super(metasitory, master, Log.class);
    }

    public void apply(Provider<?> loggerProvider) {
        for (LogMetacode metacode : metacodes)
            metacode.apply(master, loggerProvider);
    }

    public interface LogMetacode extends MasterMetacode<Object> {
        public void apply(Object master, Provider<?> loggerProvider);
    }
}
