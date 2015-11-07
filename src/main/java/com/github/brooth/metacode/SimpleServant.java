package com.github.brooth.metacode;

public abstract class SimpleServant<M, C> extends MasterServant<M> { 
	protected List<C> metacodes;

	protected MasterServant(M master) {
		this.master = master;
	}

	public void prepare(Metasitory metasitory) {
		this.metacodes = (List<C>) metasitory.search(criteria());
	}

	protected Criteria criteria() {
		return new Criteria.Builder().masterEqDeep(master.getClass()).build();
	}
}

