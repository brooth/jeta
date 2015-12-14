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

package org.brooth.jeta.samples.collector;

import org.brooth.jeta.collector.ObjectCollector;
import org.brooth.jeta.collector.TypeCollector;
import org.brooth.jeta.samples.MetaHelper;
import org.brooth.jeta.util.Provider;

import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class CollectorSample {

    @TypeCollector(MyAction.class)
    public static class ActionTypeCollector {
        public void collect() {
            List<Class<?>>  actions = MetaHelper.collectTypes(getClass(), MyAction.class);
            for (Class<?> action : actions) {
                System.out.println("action class: " + action.getCanonicalName());
            }
        }
    }

    @ObjectCollector(MyAction.class)
    public static class ActionObjectCollector {
        public void collect() {
            List<Provider<?>> actions = MetaHelper.collectObjects(getClass(), MyAction.class);
            for (Provider<?> action : actions) {
                System.out.println("action result: " + ((Action) action.get()).execute());
            }
        }
    }

    public static void main(String[] args) {
        new ActionTypeCollector().collect();
        new ActionObjectCollector().collect();
    }
}
