package org.javameta.log;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;
import org.javameta.util.Provider;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
