package org.javameta;

import org.javameta.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

public abstract class MasterController<M, C extends MasterMetacode> extends MasterClassController<M, C> {

    protected M master;

    protected MasterController(Metasitory metasitory, M master) {
        this(metasitory, master, null);
    }

    @SuppressWarnings("unchecked")
    public MasterController(Metasitory metasitory, M master, @Nullable Class<? extends Annotation> annotationClass) {
        super(metasitory, (Class<? extends M>) master.getClass(), annotationClass);
        this.master = master;
    }
}

