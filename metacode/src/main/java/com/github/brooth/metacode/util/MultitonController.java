package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterClassController;
import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.ClassForNameMetasitory;
import com.github.brooth.metacode.metasitory.Criteria;
import com.github.brooth.metacode.metasitory.Metasitory;
import com.google.common.collect.Iterables;

/**
 *
 */
public class MultitonController<M, K> extends MasterClassController<M, MasterMetacode> {

    protected Class<K> keyClass;

    public MultitonController(Class<? extends M> masterClass, Class<K> keyClass) {
        this(new ClassForNameMetasitory(), masterClass, keyClass);
    }

    public MultitonController(Metasitory metasitory, Class<? extends M> masterClass, Class<K> keyClass) {
        super(metasitory, masterClass);
        this.keyClass = keyClass;
    }

    @Override
    protected Criteria criteria() {
        return new Criteria.Builder().masterEq(masterClass).build();
    }

    public M getInstance(K key) {
        MasterMetacode multiton = Iterables.getFirst(metacodes, null);
        if (multiton == null || !(multiton instanceof MultitonMetacode))
            throw new IllegalStateException(masterClass.getCanonicalName() + " has not multiton meta code. No @Multiton annotation on it?");

        @SuppressWarnings("unchecked")
        M instance = (M) ((MultitonMetacode) multiton).getInstance(key);
        return instance;
    }

}
