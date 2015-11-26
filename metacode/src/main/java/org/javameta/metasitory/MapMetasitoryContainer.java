package org.javameta.metasitory;

import org.javameta.MasterMetacode;
import org.javameta.util.Provider;

import java.util.Map;

/**
 * 
 */
public interface MapMetasitoryContainer {

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
