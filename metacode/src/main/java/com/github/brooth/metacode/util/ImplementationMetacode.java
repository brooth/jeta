package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;

/**
 * 
 */
public interface ImplementationMetacode extends MasterMetacode {
    <I> I getImplementation(Class<I> of);
}
