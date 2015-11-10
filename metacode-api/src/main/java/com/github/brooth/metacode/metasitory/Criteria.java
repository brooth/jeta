package com.github.brooth.metacode.metasitory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 *
 */
public final class Criteria {
    @Nullable
    private Class masterEq;
    @Nullable
    private Class masterEqDeep;
    @Nullable
    private Class masterInstanceOf;
    @Nullable
    private List<Class<? extends Annotation>> usesAny;
    @Nullable
    private List<Class<? extends Annotation>> usesAll;

    private Criteria() {

    }

    public static class Builder {
        Criteria criteria = new Criteria();

        public Criteria build() {
            return criteria;
        }

        public Builder masterEq(Class value) {
            criteria.masterEq = value;
            return this;
        }

        public Builder masterEqDeep(Class value) {
            criteria.masterEqDeep = value;
            return this;
        }

        public Builder masterInstanceOf(Class value) {
            criteria.masterInstanceOf = value;
            return this;
        }

        public Builder usesAny(List<Class<? extends Annotation>> value) {
            criteria.usesAny = value;
            return this;
        }

        public Builder usesAny(Class<? extends Annotation> value) {
            if (criteria.usesAny == null)
                criteria.usesAny = new ArrayList<>();
            criteria.usesAny.add(value);
            return this;
        }

        public Builder usesAll(List<Class<? extends Annotation>> value) {
            criteria.usesAll = value;
            return this;
        }
    }

    @Nullable
    public Class getMasterEq() {
        return masterEq;
    }

    @Nullable
    public Class getMasterEqDeep() {
        return masterEqDeep;
    }

    @Nullable
    public Class getMasterInstanceOf() {
        return masterInstanceOf;
    }

    @Nullable
    public List<Class<? extends Annotation>> getUsesAny() {
        return usesAny;
    }

    @Nullable
    public List<Class<? extends Annotation>> getUsesAll() {
        return usesAll;
    }
}


