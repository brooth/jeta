package org.javameta.pubsub;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public @interface Subscribe {

    Class<?>[] value();

    Class<? extends Filter>[] filters() default {};

    int[] ids() default {};

    String[] topics() default {};

    int priority() default 0;
}
