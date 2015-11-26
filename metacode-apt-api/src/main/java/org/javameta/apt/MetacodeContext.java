package org.javameta.apt;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface MetacodeContext {
    /*
     * com.example.app
     */
    public String getMasterPackage();

    /*
     * com.example.app.Foo.Boo
     */
    public String getMasterCanonicalName();

    /*
     * Boo
     */
    public String getMasterSimpleName();

    public Set<Class<? extends Annotation>> metacodeAnnotations();
}   
