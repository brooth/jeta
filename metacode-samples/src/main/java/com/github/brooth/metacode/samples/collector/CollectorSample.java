package com.github.brooth.metacode.samples.collector;

import com.github.brooth.metacode.samples.MetaHelper;
import com.github.brooth.metacode.collector.*;
import java.util.List;
import com.github.brooth.metacode.util.Provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 */
public class CollectorSample {

    @Target(ElementType.TYPE)
    public @interface Handler {

    }

    interface IHandler {
        String handle();       
    }

    @Handler
    public class HandlerOne implements IHandler {
        @Override
        public String handle() {
            return "one";
        }
    }

    @Handler
    public class HandlerTwo implements IHandler {
        @Override
        public String handle() {
            return "two";
        }
    }

    @TypeCollector(Handler.class)
    public static class HandlerClassCollector {
        public void collectAndRun() {
            List<Class> handlers = MetaHelper.collectTypes(getClass(), Handler.class);
            for (Class handlerClass : handlers) {
                System.out.println("handler: " + handlerClass.getCanonicalName());
            }
        }
    }

    @ObjectCollector(Handler.class)
    public static class HandlerObjectCollector {
        public void collectAndRun() {
            List<Provider<?>> handlers = MetaHelper.collectObjects(getClass(), Handler.class);
            for (Provider<?> provider : handlers) {
                System.out.println("handler result: " + ((IHandler) provider.get()).handle());
            }
        }
    }

    public static void main(String[] args) {
        new HandlerClassCollector().collectAndRun();
        new HandlerObjectCollector().collectAndRun();
    }
}
