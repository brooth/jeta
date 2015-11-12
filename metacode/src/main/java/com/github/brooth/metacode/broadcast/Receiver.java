package com.github.brooth.metacode.broadcast;

/**
 *  
 */
public @interface Receiver {

	Class<? extends Filter>[] filters() default {};

	int[] ids() default {};

	String[] tags() default {};

    String filterExpression() default "";

	int priority() default 0;
}
