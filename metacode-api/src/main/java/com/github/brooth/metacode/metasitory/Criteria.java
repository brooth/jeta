package com.github.brooth.metacode.metasitory;

import java.lang.annotation.Annotation;

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
    private Class<? extends Annotation> usesAnnotation;
    @Nullable
    private Class<? extends Annotation>[] usesAny;
    @Nullable
    private Class<? extends Annotation>[] usesAll;

    public static class Builder {
        public Criteria build() {
            return null;
        }

        public Builder masterEqDeep(Class value) {
            return this;
        }

        public Builder usesAnnotation(Class<? extends Annotation> value) {
            return this;
        }
    }

    public Class getMasterEq() {
        return masterEq;
    }

    public Class getMasterEqDeep() {
        return masterEqDeep;
    }

    public Class getMasterInstanceOf() {
        return masterInstanceOf;
    }

    public Class<? extends Annotation> getUsesAnnotation() {
        return usesAnnotation;
    }

    public Class<? extends Annotation>[] getUsesAny() {
        return usesAny;
    }

    public Class<? extends Annotation>[] getUsesAll() {
        return usesAll;
    }
}


