package org.brooth.jeta.inject;

/**
 * @author khalidov
 * @version $Id$
 */
public interface IMetaEntity<E> {
    Class<? extends E> getEntityClass();
    boolean isImplemented();
}
