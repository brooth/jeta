package com.github.brooth.metacode.metasitory;

/*
 * read-only
 */
public final class Criteria {
	@Nullable
	private Class masterEq;
	@Nullable
	private Class masterEqDeep;
	@Nullable
	private Class masterInstanceOf;
	@Nullable
	private Class<? extends Annotation> usesAnnotation;
	@Nullable
	private Class<? extends Annotation>[] usesAny;
	@Nullable
	private Class<? extends Annotation>[] usesAll;

  	// todo: builder and getters
}


