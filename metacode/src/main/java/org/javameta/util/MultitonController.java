package org.javameta.util;

import com.google.common.collect.Iterables;
import org.javameta.MasterClassController;
import org.javameta.MasterMetacode;
import org.javameta.metasitory.ClassForNameMetasitory;
import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
