package com.github.brooth.metacode;

/*
 * todo move to samples
 */
public class Metacode {	

	private Metasitory metasitory;

	public Metacode(String metapackage) {
		metasitory = new HashMapMetasitory(metapackage);
	}

	public void applyLogs(Object master) {
		new LogServant(metasitory, master).apply();
	}

	public <M> ImplementationServant<M>(M master) {
		return new ImplementationServant(metasitory, master);
	}

	// others
}
