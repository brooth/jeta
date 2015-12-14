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

package org.brooth.jeta.samples.singleton;

import org.brooth.jeta.samples.MetaHelper;
import org.brooth.jeta.util.Singleton;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonSample {

    @Singleton(staticConstructor = "newInstance")
    static SingletonSample singleton;

    public static SingletonSample getInstance() {
        if (singleton == null)
            MetaHelper.createSingleton(SingletonSample.class);

        return singleton;
    }

    static SingletonSample newInstance() {
        if (singleton != null)
            throw new IllegalStateException("Jeta sucks?");

        return new SingletonSample();
    }

    public static void main(String[] args) {
        System.out.println(getInstance() == getInstance());
    }
}
