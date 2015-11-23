package com.github.brooth.metacode.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author khalidov
 * @version $Id$
 */
@Target(ElementType.FIELD)
public @interface Proxy {
    Class<?> value();
}
