/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.javameta.tests.singleton;

import org.javameta.BaseTest;
import org.javameta.Logger;
import org.javameta.TestMetaHelper;
import org.javameta.log.Log;
import org.javameta.util.Multiton;
import org.javameta.util.Singleton;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonTest extends BaseTest {

    @Log
    Logger logger;

    public static class SingletonHolder {
        @Singleton
        static SingletonHolder instance;

        SingletonHolder() {
            assertNull(instance);
        }

        private static SingletonHolder getInstance() {
            if (instance == null)
                TestMetaHelper.createSingleton(SingletonHolder.class);

            return instance;
        }
    }

    public static class MultitonHolder {
        @Multiton(staticConstructor = "newInstance")
        static Map<Object, MultitonHolder> multiton = new HashMap<>();

        private String key;

        public static MultitonHolder getInstance(String key) {
            if (!multiton.containsKey(key))
                TestMetaHelper.createMultitonInstance(MultitonHolder.class, key);

            return multiton.get(key);
        }

        static MultitonHolder newInstance(Object key) {
            assertFalse(multiton.containsKey(key));
            MultitonHolder holder = new MultitonHolder();
            holder.key = (String) key;
            return holder;
        }

        public String getKey() {
            return key;
        }
    }

    @Test
    public void testSingleton() {
        logger.debug("testSingleton()");

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    SingletonHolder instance = SingletonHolder.getInstance();
                    assertNotNull(instance);
                    logger.debug("[%s] instance: %s", Thread.currentThread().getName(), instance.toString());
                }
            });
        }

        for (Thread thread : threads)
            thread.start();

        sleepQuietly(500);
    }

    @Test
    public void testMultiton() {
        logger.debug("testMultiton()");

        Thread[] oneThreads = new Thread[10];
        Thread[] twoThreads = new Thread[oneThreads.length];

        for (int i = 0; i < oneThreads.length; i++) {
            oneThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    MultitonHolder instance = MultitonHolder.getInstance("one");
                    assertNotNull(instance);
                    logger.debug("[%s] instance: %s, key: %s", Thread.currentThread().getName(), instance.toString(), instance.getKey());
                }
            });
            twoThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    MultitonHolder instance = MultitonHolder.getInstance("two");
                    assertNotNull(instance);
                    logger.debug("[%s] instance: %s, key: %s", Thread.currentThread().getName(), instance.toString(), instance.getKey());
                }
            });
        }

        for (int i = 0; i < oneThreads.length; i++) {
            oneThreads[i].start();
            twoThreads[i].start();
        }

        sleepQuietly(500);
    }
}
