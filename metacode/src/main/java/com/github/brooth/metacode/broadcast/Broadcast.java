package com.github.brooth.metacode.broadcast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *  
 */
@Target(ElementType.TYPE)
public @interface Broadcast {
	Class<? extends Message> value();
}
