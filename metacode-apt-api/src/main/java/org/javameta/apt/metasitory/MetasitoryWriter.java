package org.javameta.apt.metasitory;

import org.javameta.apt.MetacodeContext;

import javax.annotation.processing.ProcessingEnvironment;

/**
 *
 */
public interface MetasitoryWriter {
    void open(ProcessingEnvironment env);
    void write(MetacodeContext context);
    void close();
}
