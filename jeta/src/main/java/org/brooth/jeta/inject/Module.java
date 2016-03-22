package org.brooth.jeta.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author khalidov
 * @version $Id$
 */
@Target(ElementType.TYPE)
public @interface Module {
    Class<?>[] scopes();
}
