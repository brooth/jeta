package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterClassServant;
import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

/**
 * @author khalidov
 * @version $Id$
 */
public class ImplementationServant<I> extends MasterClassServant<Object, ImplementationServant.ImplementationMetacode> {

    public ImplementationServant(Metasitory metasitory, Class<?> masterClass) {
        super(metasitory, masterClass);
    }

    @Nullable
    public I getImplementation(Class<I> of) {
        ImplementationMetacode first = Iterables.getFirst(metacodes, null);
        return first == null ? null : first.getImplementation(of);
    }

    public interface ImplementationMetacode extends MasterMetacode {
        <I> I getImplementation(Class<I> of);
    }
}
