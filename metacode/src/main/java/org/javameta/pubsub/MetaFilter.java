package org.javameta.pubsub;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
