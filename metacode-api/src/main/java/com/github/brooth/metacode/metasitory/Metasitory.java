package com.github.brooth.metacode.metasitory;

public interface Metasitory {
	List<Record> search(Criteria criteria);
}

public class HashMapMetasitory implements Metasitory {
	// HashMap implementation
}

