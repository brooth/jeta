package org.brooth.jeta.meta;

/**
 * @author khalidov
 * @version $Id$
 */
public interface Scope {

    final class Default implements Scope {
        private static final Default instance = new Default();

        private Default() {
        }

        public static Default getInstance() {
            return instance;
        }
    }
}
