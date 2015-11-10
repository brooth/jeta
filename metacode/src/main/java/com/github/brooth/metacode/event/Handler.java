package com.github.brooth.metacode.event;

/**
 *  
 */
public @interface Handler {

	Class<? extends Filter>[] filters() default {};

	int[] filterIds() default {};

	String[] filterTags() default {};

    String filterExpression() default "";

	int priority() default 0;
}
