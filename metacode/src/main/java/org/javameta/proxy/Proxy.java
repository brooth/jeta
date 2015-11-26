package org.javameta.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Target(ElementType.FIELD)
public @interface Proxy {
    Class<?> value();
}
