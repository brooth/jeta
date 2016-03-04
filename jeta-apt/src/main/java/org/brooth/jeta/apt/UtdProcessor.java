package org.brooth.jeta.apt;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface UtdProcessor extends Processor {

    /**
     * If masters source code hasn't been changed since its meta code generated,
     * return false to avoid regenerating
     */
    boolean ignoreUpToDate();
}
