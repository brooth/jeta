package org.javameta.util;

import org.javameta.MasterMetacode;

/**
 * 
 */
public interface ImplementationMetacode extends MasterMetacode {
    <I> I getImplementation(Class<I> of);
}
