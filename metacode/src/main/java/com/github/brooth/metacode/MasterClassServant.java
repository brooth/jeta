package com.github.brooth.metacode;

import com.github.brooth.metacode.metasitory.Criteria;
import com.github.brooth.metacode.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @param <M>
 * @param <C>
 */
public abstract class MasterClassServant<M, C extends MasterMetacode> {

    protected Class<? extends M> masterClass;
    protected List<C> metacodes;
    @Nullable
    protected Class<? extends Annotation> annotationClass;

    @SuppressWarnings("unchecked")
    protected MasterClassServant(Metasitory metasitory, Class<? extends M> masterClass) {
        this.masterClass = masterClass;
        this.metacodes = (List<C>) metasitory.search(criteria());
    }

    public MasterClassServant(Metasitory metasitory, Class<? extends M> masterClass, @Nullable Class<? extends Annotation> annotationClass) {
        this(metasitory, masterClass);
        this.annotationClass = annotationClass;
    }

    protected Criteria criteria() {
        Criteria.Builder builder = new Criteria.Builder().masterEqDeep(masterClass);
        if (annotationClass != null)
            builder.usesAnnotation(annotationClass);
        return builder.build();
    }
}

