package com.github.brooth.metacode.pubsub;

/**
 *
 */
public @interface MetaFilter {

    /**
     * %m - master
     * %e - event
     *
     * @return filter expression
     */
    String emitExpression();
}
