package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;

import java.util.Collections;
import java.util.List;

public class ClassForNameMetasitory implements Metasitory {

    private Class metacodeClass;

    public ClassForNameMetasitory(Class masterClass) {
        try {
            metacodeClass = Class.forName(masterClass.getName() + "_Metacode");

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class " + masterClass, e);
        }
    }

    @Override
    public List<MasterMetacode<?>> search(Criteria c) {
        try {
            MasterMetacode<?> instance = (MasterMetacode) metacodeClass.newInstance();
            return Collections.<MasterMetacode<?>>singletonList(instance);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initiate class " + metacodeClass, e);
        }
    }
}

