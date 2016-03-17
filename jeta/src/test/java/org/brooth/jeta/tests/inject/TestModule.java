package org.brooth.jeta.tests.inject;

import org.brooth.jeta.inject.Module;

/**
 * @author khalidov
 * @version $Id$
 */
@Module(scopes = {CustomScope.class, DefaultScope.class, ExtScope.class, ExtExtScope.class})
public interface TestModule {
}
