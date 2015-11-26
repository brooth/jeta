package org.javameta.pubsub;

/**
 *
 */
public @interface MetaFilter {

    /**
     * $m - master
     * $e - event
     *
     * @return filter expression
     */
    String emitExpression();
}
