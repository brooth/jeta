package org.brooth.jeta.inject;

/**
 * @author khalidov
 * @version $Id$
 */
public @interface Scope {
    Class<?> assignable() default Void.class;
}
