package org.javameta.tests.implementation;

import org.javameta.util.Implementation;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Implementation(value = AbstractPriority.class, priority = Integer.MIN_VALUE, staticConstructor = "newInstance")
public class PriorityLowImplementer extends AbstractPriority {

    private PriorityLowImplementer() {

    }

    public static PriorityLowImplementer newInstance() {
        return new PriorityLowImplementer();
    }

    @Override
    public String value() {
        return "low but via static constructor";
    }
}
