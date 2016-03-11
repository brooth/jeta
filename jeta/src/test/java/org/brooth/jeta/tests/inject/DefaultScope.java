package org.brooth.jeta.tests.inject;

import org.brooth.jeta.inject.Scope;

/**
 * @author khalidov
 * @version $Id$
 */
@Scope
public class DefaultScope {
    public String getData() {
        return "default scope data";
    }
}
