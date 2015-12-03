package org.javameta.tests.implementation;

import org.javameta.util.Implementation;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Implementation(value = AbstractPriority.class, priority = Integer.MAX_VALUE)
public class PriorityHighImplementer extends AbstractPriority {

    @Override
    public String value() {
        return "winner";
    }
}
