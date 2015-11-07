package com.github.brooth.metacode;

/*
 * needs a master to apply to and handles one type of metacode
 * todo: BaseMasterServant with masterEqDeep criteria. also for solid
 */
public abstract class MasterServant<M> implements Servant<M> {
	protected M master;

	protected MasterServant(M master) {
		this.master = master;
	}
}
