package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.log.NamedLogger;
import com.github.brooth.metacode.metasitory.Metasitory;

import javax.inject.Provider;

/**
 * @author khalidov
 * @version $Id$
 */
public class LogServant extends MasterServant<Object, LogServant.LogMetacode<Object>> {

    public LogServant(Metasitory metasitory, Object master) {
        super(metasitory, master);
    }

    public void apply(Provider<NamedLogger> loggerProvider) {
        for (LogMetacode metacode : metacodes)
            metacode.apply(loggerProvider, master);
    }

    public interface LogMetacode<M> extends MasterMetacode {
        public void apply(Provider<NamedLogger> loggerProvider, M master);
    }
}
