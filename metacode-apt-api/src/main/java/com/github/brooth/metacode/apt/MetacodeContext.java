package com.github.brooth.metacode.apt;

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

    // package livel, in MetacodeContextImpl;
    // JavaWriter writer();

    // package livel, in MetacodeContextImpl;
    //Map<BaseProcessor, ProcessorContext> processors();
}   
