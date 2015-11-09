package com.github.brooth.metacode;

import com.github.brooth.metacode.metasitory.Metasitory;

public abstract class MasterServant<M, C> extends MasterClassServant<M, C> {
	protected M master;

	protected MasterServant(Metasitory metasitory, M master) {
		super(metasitory, (Class<? extends M>) master.getClass());
		this.master = master;
	}
}

