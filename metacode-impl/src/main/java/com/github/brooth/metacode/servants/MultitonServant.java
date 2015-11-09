package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterClassServant;
import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class MultitonServant<M, C> extends MasterClassServant<M, C> {

    public MultitonServant(Metasitory metasitory, Class<? extends M> masterClass) {
        super(metasitory, masterClass);
    }

    public HashMapMetasitory getInstance(String metapackage) {
        return null;
    }
}
