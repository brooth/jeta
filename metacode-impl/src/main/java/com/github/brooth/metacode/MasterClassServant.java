package com.github.brooth.metacode;

public abstract class MasterClassServant<M, C> { 
	protected Class<? extends M> masterClass;
	protected List<C> metacodes;

	protected MasterServant(Metasitory metasitory, Class<? extends M> masterClass) {
		this.masterClass<? extends M> = masterClass;
		this.metacodes = (List<C>) metasitory.search(criteria());
	}

	protected Criteria criteria() {
		return new Criteria.Builder().masterEqDeep(masterClass).build();
	}
}

