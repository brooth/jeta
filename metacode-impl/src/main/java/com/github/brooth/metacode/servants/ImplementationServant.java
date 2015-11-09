package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterClassServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ImplementationServant<M> extends MasterClassServant<M, Object> {

    public ImplementationServant(Metasitory metasitory, Class<? extends M> masterClass) {
        super(metasitory, masterClass);
    }
}
