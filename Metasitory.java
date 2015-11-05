//-----------------------------------------------
//                  APT
//-----------------------------------------------
package com.github.brooth.metacode.apt.metasitory;

public interface MetasitoryWriter {
	void open(Env env);
	void write(Record r);
	void close();	
}

//-----------------------------------------------
//                MAIN
//-----------------------------------------------
package com.github.brooth.metacode.metasitory;

public final class Record {
	private final String host;
	private final String metacode;
	private final String[] annotations;
}

public final class Criteria {
	@Nullable
	private Class masterEq;
	@Nullable
	private Class masterEqDeep;
	@Nullable
	private Class masterInstanceOf;
	@Nullable
	private Class<? extends Annotation> uses;
	@Nullable
	private Class<? extends Annotation>[] usesAny;
	@Nullable
	private Class<? extends Annotation>[] usesAll;
	private maxSize = -1;
}

public interface Metasitory {
	void open();
	List<Record> search(MetaFinder finder);
	void close();
}

public class HashMapMetasitory implements Metasitory {
	// HashMap implementation
}

