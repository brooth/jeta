package org.brooth.jeta.apt;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface UtdMetacodeContext extends MetacodeContext {

    /**
     * true if masters source code hasn't been changed since metacode generated
     */
    boolean isUpToDate();
}
