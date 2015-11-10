package com.github.brooth.metacode.apt.metasitory;

import javax.annotation.processing.ProcessingEnvironment;

/**
 *
 */
public interface MetasitoryWriter {
    void open(ProcessingEnvironment env);
    void write(Record r);
    void close();	
}
