package org.javameta.pubsub;

/**
 *
 */
public @interface Subscribe {

    Class<?>[] value();

    Class<? extends Filter>[] filters() default {};

    int[] ids() default {};

    String[] topics() default {};

    int priority() default 0;
}
