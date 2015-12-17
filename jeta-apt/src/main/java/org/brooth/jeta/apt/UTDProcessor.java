package org.brooth.jeta.apt;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface UtdProcessor extends Processor {

    /**
     * No mater if masters source code hasn't been changed since its meta code generated,
     * return true to rebuild it
     */
    boolean ignoreMasterUpToDate(ProcessorEnvironment env);

}
