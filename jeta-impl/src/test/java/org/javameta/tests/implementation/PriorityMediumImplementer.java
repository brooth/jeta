package org.javameta.tests.implementation;

import org.javameta.util.Implementation;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@Implementation(value = AbstractPriority.class)
public class PriorityMediumImplementer extends AbstractPriority {

    @Override
    public String value() {
        return null;
    }
}
