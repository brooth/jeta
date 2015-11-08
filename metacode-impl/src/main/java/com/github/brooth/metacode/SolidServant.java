package com.github.brooth.metacode;

/*
 * ready to apply servant. doesn't need extra args. doesn't return result
 * like logger, injector, findviws and more
 */
public abstract class SolidServant<M, C> extends SimpleServant<M, C> {
	void abstract apply();
}

