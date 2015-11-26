/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javameta.samples.collector;

import org.javameta.collector.ObjectCollector;
import org.javameta.collector.TypeCollector;
import org.javameta.samples.MetaHelper;
import org.javameta.util.Provider;

import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
        @MyAction
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
                System.out.println("action class: " + handlerClass.getCanonicalName());
            }
        }
    }

    @ObjectCollector(MyAction.class)
    public static class HandlerObjectCollector {
        public void collect() {
            List<Provider<?>> handlers = MetaHelper.collectObjects(getClass(), MyAction.class);
            for (Provider<?> provider : handlers) {
                System.out.println("action result: " + ((Action) provider.get()).execute());
            }
        }
    }

    public static void main(String[] args) {
        new HandlerClassCollector().collect();
        new HandlerObjectCollector().collect();
    }
}
