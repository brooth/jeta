package org.brooth.jeta.apt;

import java.io.File;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface UtdProcessor extends Processor {

    /**
     * If masters source code hasn't been changed since its meta code generated,
     * return true to avoid regenerating
     */
    boolean isUpToDate(ProcessorEnvironment env, File masterSourceJavaFile, long prevGenLastModified);
}
