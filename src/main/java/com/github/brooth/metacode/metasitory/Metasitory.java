package com.github.brooth.metacode.metasitory;

public interface Metasitory {
	void open();
	List<Record> search(Criteria criteria);
	void close();
}

public class HashMapMetasitory implements Metasitory {
	// HashMap implementation
}

