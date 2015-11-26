package org.javameta.metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to search metacodes in a metasitory
 */
public final class Criteria {
    /**
     * Metasitory implementation should check its compatibility with criteria by this constant
     * and throw IllegalArgumentException if this value is higher
     */
    public static final int VERSION = 1;

    private Class masterEq;

    private Class masterAssignableFrom;

    private Class masterAssignableTo;

    private Set<Class<? extends Annotation>> usesAny;

    private Set<Class<? extends Annotation>> usesAll;

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

        public Builder masterAssignableFrom(Class value) {
            criteria.masterAssignableFrom = value;
            return this;
        }

        public Builder masterAssignableTo(Class value) {
            criteria.masterAssignableTo = value;
            return this;
        }

        public Builder usesAny(Set<Class<? extends Annotation>> value) {
            criteria.usesAny = value;
            return this;
        }

        public Builder usesAny(Class<? extends Annotation> value) {
            if (criteria.usesAny == null)
                criteria.usesAny = new HashSet<>();
            criteria.usesAny.add(value);
            return this;
        }

        public Builder usesAll(Set<Class<? extends Annotation>> value) {
            criteria.usesAll = value;
            return this;
        }
    }

    @Nullable
    public Class getMasterEq() {
        return masterEq;
    }

    @Nullable
    public Class getMasterAssignableFrom() {
        return masterAssignableFrom;
    }

    @Nullable
    public Class getMasterAssignableTo() {
        return masterAssignableTo;
    }

    @Nullable
    public Set<Class<? extends Annotation>> getUsesAny() {
        return usesAny;
    }

    @Nullable
    public Set<Class<? extends Annotation>> getUsesAll() {
        return usesAll;
    }
}


