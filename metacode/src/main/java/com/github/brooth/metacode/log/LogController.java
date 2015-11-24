package com.github.brooth.metacode.log;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

import javax.inject.Provider;

/**
 *
 */
public class LogController extends MasterController<Object, LogMetacode> {

    public LogController(Metasitory metasitory, Object master) {
        super(metasitory, master, Log.class);
    }

    public void apply(Provider<?> loggerProvider) {
        for (LogMetacode metacode : metacodes)
            metacode.apply(master, loggerProvider);
    }

}
