package org.brooth.jeta.tests.inject;

import org.brooth.jeta.inject.Scope;

/**
 * @author khalidov
 * @version $Id$
 */
@Scope
public class CustomScope {
    public String getData() {
        return "custom scope data";
    }
}
