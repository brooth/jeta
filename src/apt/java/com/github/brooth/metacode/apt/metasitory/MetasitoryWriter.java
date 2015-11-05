package com.github.brooth.metacode.apt.metasitory;

public interface MetasitoryWriter {
	void open(ProcessingEnv env);
	void write(Record r);
	void close();	
}


