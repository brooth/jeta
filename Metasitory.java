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

public interface Metasitory {
	void open();
	List<Record> search(@Nullable host, @Nullable metacode, @Nullable annotation);
	void close();
}

public class HashMapMetasitory implements Metasitory {}

