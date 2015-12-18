package org.brooth.jeta.apt;

import java.io.File;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface UtdProcessor extends Processor {

    /**
     * If masters source code hasn't been changed since its meta code generated,
     * return false to avoid regenerating
     */
    public boolean ignoreUpToDate(ProcessorEnvironment env);
}
