package com.github.brooth.metacode;

import com.github.brooth.metacode.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

public abstract class MasterServant<M, C extends MasterMetacode> extends MasterClassServant<M, C> {

    protected M master;

    protected MasterServant(Metasitory metasitory, M master) {
        this(metasitory, master, null);
    }

    @SuppressWarnings("unchecked")
    public MasterServant(Metasitory metasitory, M master, @Nullable Class<? extends Annotation> annotationClass) {
        super(metasitory, (Class<? extends M>) master.getClass(), annotationClass);
        this.master = master;
    }
}

