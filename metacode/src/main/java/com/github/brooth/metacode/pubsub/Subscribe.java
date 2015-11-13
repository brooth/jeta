package com.github.brooth.metacode.pubsub;

/**
 *  
 */
public @interface Subscribe {

	Class<?>[] value();

	Class<? extends Filter>[] filters() default {};

	int[] ids() default {};

	String[] topics() default {};

    String filterExpression() default "";

	int priority() default 0;
}
