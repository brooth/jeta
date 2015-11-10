package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;

import java.util.Collections;
import java.util.List;

public class ClassForNameMetasitory implements Metasitory {

    @Override
    public List<MasterMetacode> search(Criteria c) {
        if (c.getMasterEqDeep() != null)
            throw new UnsupportedOperationException("Criteria.masterEqDeep not supported. Criteria.masterEq only.");
        if (c.getMasterInstanceOf() != null)
            throw new UnsupportedOperationException("Criteria.masterInstanceOf not supported. Criteria.masterEq only.");
        if (c.getUsesAny() != null)
            throw new UnsupportedOperationException("Criteria.usesAny not supported. Criteria.masterEq only.");
        if (c.getUsesAll() != null)
            throw new UnsupportedOperationException("Criteria.usesAll not supported. Criteria.masterEq only.");
        if (c.getMasterEq() == null)
            throw new UnsupportedOperationException("Criteria.masterEq not present.");

        Class<?> metacodeClass;
        try {
            metacodeClass = Class.forName(c.getMasterEq().getName() + "_Metacode");

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class " + c.getMasterEq(), e);
        }

        try {
            MasterMetacode<?> instance = (MasterMetacode) metacodeClass.newInstance();
            return Collections.<MasterMetacode>singletonList(instance);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initiate class " + metacodeClass, e);
        }
    }
}

