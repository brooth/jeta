package org.brooth.jeta.tests.inject;

import org.brooth.jeta.inject.Scope;

/**
 * @author khalidov
 * @version $Id$
 */
@Scope(assignable = DefaultScope.class)
public class ExtScope {
    public String data = "assignable from default scope";
}
