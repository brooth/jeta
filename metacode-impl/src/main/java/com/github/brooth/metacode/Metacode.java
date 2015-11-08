package com.github.brooth.metacode;

public class Metacode {	

	private Metasitory metasitory;

	public Metacode(String metapackage) {
		metasitory = new HashMapMetasitory(metapackage);
	}

	public void applyLogs(Object master) {
		new LogServant(metasitory, master).apply();
	}

	public <M> InstanceServant<M>(M master) {
		return new InstanceServant(metasitory, master);
	}

	// others
}
