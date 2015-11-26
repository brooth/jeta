package org.javameta.util;

/**
 * 
 */
public interface ImplementationMetacode {
    <I> I getImplementation(Class<I> of);
}
