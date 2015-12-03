package org.javameta.tests.implementation;

import org.javameta.util.Implementation;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Implementation(AbstractFoo.class)
public class FooImplementer extends AbstractFoo {

    @Override
    public String value() {
        return "impl";
    }
}
