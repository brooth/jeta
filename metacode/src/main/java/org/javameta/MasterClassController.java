package org.javameta;

import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @param <M> master's class
 * @param <C> metacode extension
 */
public abstract class MasterClassController<M, C extends MasterMetacode> {

    protected Class<? extends M> masterClass;
    protected List<C> metacodes;
    @Nullable
    protected Class<? extends Annotation> annotationClass;

    protected MasterClassController(Metasitory metasitory, Class<? extends M> masterClass) {
        this.masterClass = masterClass;
        searchMetacodes(metasitory);
    }

    public MasterClassController(Metasitory metasitory, Class<? extends M> masterClass, @Nullable Class<? extends Annotation> annotationClass) {
        this(metasitory, masterClass);
        this.annotationClass = annotationClass;
    }

    @SuppressWarnings("unchecked")
    protected void searchMetacodes(Metasitory metasitory) {
        this.metacodes = (List<C>) metasitory.search(criteria());
    }

    protected Criteria criteria() {
        Criteria.Builder builder = new Criteria.Builder().masterAssignableTo(masterClass);
        if (annotationClass != null)
            builder.usesAny(annotationClass);
        return builder.build();
    }
}

