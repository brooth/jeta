package com.github.brooth.metacode;

public abstract class MasterServant<M, C> extends MasterClassServant<M, C> { 
	protected M master;

	protected MasterServant(Metasitory metasitory, M master) {
		super(metasitory, master.getClass());
		this.master = master;
	}
}

