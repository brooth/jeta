package com.github.brooth.metacode.pubsub;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *  
 */
@Target(ElementType.TYPE)
public @interface Publisher {
	Class<? extends Message> value();
}
