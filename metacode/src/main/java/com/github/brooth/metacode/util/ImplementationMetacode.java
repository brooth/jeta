package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;

/**
* @author khalidov
* @version $Id$
*/
public interface ImplementationMetacode extends MasterMetacode {
    <I> I getImplementation(Class<I> of);
}
