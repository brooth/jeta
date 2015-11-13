package com.github.brooth.metacode.apt;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 *
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

    /*
     * Boo_Metacode
     */
    public String getMetacodeSimpleName();

    /*
     * com.example.app.Foo$Boo
     */
    public String getMasterFlatName();

    /*
     * com.example.app.Foo
     */
    public String getSourceCanonicalName();

    public Set<Class<? extends Annotation>> metacodeAnnotations();

}   
