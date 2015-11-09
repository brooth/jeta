package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.log.NamedLogger;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class LogServant extends MasterServant<Object, NamedLogger> {

    public LogServant(Metasitory metasitory, Object master) {
        super(metasitory, master);
    }

    public void apply() {

    }
}
