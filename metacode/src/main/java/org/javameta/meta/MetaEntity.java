package org.javameta.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
public @interface MetaEntity {

    Class of() default Void.class;

    Class ext() default Void.class;

    String staticConstructor() default "";

    boolean minor() default false;

    int priority() default 0;
}
