package org.javameta.samples.collector;

import org.javameta.collector.ObjectCollector;
import org.javameta.collector.TypeCollector;
import org.javameta.samples.MetaHelper;
import org.javameta.util.Provider;

import java.util.List;

/**
 *
 */
public class CollectorSample {

    public @interface MyAction {

    }

    interface Action {
        String execute();
    }

    @MyAction
    public static class ActionOne implements Action {
        @Override
        public String execute() {
            return "one";
        }
    }

    @MyAction
    public static class ActionTwo implements Action {
        @Override
        public String execute() {
            return "two";
        }
    }

    @TypeCollector(MyAction.class)
    public static class HandlerClassCollector {
        public void collect() {
            List<Class> handlers = MetaHelper.collectTypes(getClass(), MyAction.class);
            for (Class handlerClass : handlers) {
                System.out.println("handler: " + handlerClass.getCanonicalName());
            }
        }
    }

    @ObjectCollector(MyAction.class)
    public static class HandlerObjectCollector {
        public void collect() {
            List<Provider<?>> handlers = MetaHelper.collectObjects(getClass(), MyAction.class);
            for (Provider<?> provider : handlers) {
                System.out.println("handler result: " + ((Action) provider.get()).execute());
            }
        }
    }

    public static void main(String[] args) {
        new HandlerClassCollector().collect();
        new HandlerObjectCollector().collect();
    }
}
