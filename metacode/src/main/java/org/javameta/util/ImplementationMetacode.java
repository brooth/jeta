package org.javameta.util;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface ImplementationMetacode {
    <I> I getImplementation(Class<I> of);
}
