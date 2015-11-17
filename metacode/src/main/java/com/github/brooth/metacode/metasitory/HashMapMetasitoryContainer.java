package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;

import javax.inject.Provider;
import java.util.Map;

/**
 * @author khalidov
 * @version $Id$
 */
public interface HashMapMetasitoryContainer {

    public Map<Class, Context> get();

    public final class Context {
        public final Class masterClass;
        public final Provider<? extends MasterMetacode> metacodeProvider;
        public final Class[] annotations;

        public Context(Class masterClass, Provider<? extends MasterMetacode> metacodeProvider, Class[] annotations) {
            this.masterClass = masterClass;
            this.metacodeProvider = metacodeProvider;
            this.annotations = annotations;
        }
    }
}
