package com.github.brooth.metacode.apt.metasitory;

import com.github.brooth.metacode.apt.MetacodeContext;

import javax.annotation.processing.ProcessingEnvironment;

/**
 *
 */
public interface MetasitoryWriter {
    void open(ProcessingEnvironment env);
    void write(MetacodeContext context);
    void close();
}
