package com.github.brooth.metacode.servants;

import com.github.brooth.metacode.MasterClassServant;
import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.ClassForNameMetasitory;
import com.github.brooth.metacode.metasitory.Criteria;
import com.github.brooth.metacode.metasitory.HashMapMetasitory;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.github.brooth.metacode.util.Multiton;
import com.google.common.collect.Iterables;

/**
 * @author khalidov
 * @version $Id$
 */
public class MultitonServant<M, K> extends MasterClassServant<M, MultitonServant.MultitonMetacode> {

    protected Class<K> keyClass;

    public MultitonServant(Class<? extends M> masterClass, Class<K> keyClass) {
        this(new ClassForNameMetasitory(masterClass), masterClass, keyClass);
    }

    public MultitonServant(Metasitory metasitory, Class<? extends M> masterClass, Class<K> keyClass) {
        super(metasitory, masterClass, Multiton.class);
        this.keyClass = keyClass;
    }

    @Override
    protected Criteria criteria() {
        return new Criteria.Builder().masterEq(masterClass).usesAny(Multiton.class).build();
    }

    public M getInstance(K key) {
        MultitonMetacode multiton = Iterables.getFirst(metacodes, null);
        if (multiton == null)
            throw new IllegalStateException(masterClass.getCanonicalName() + " has not multiton meta code. No @Multiton annotation on it?");

        @SuppressWarnings("unchecked")
        M instance = (M) multiton.getInstance(key);
        return instance;
    }

    public interface MultitonMetacode<M, K> extends MasterMetacode<M> {
        M getInstance(K key);
    }
}
