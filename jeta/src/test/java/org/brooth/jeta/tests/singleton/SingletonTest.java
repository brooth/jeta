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

package org.brooth.jeta.tests.singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.TestMetaHelper;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.util.Multiton;
import org.brooth.jeta.util.MultitonMetacode;
import org.brooth.jeta.util.Singleton;
import org.brooth.jeta.util.SingletonMetacode;
import org.junit.Test;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonTest extends BaseTest {

    @Log
    Logger logger;

    @Singleton
    public static class SingletonHolder {
        static SingletonMetacode<SingletonHolder> singleton = 
            TestMetaHelper.getSingleton(SingletonHolder.class);

        static volatile int instances = 0;

        SingletonHolder() {
            assertThat(instances++, is(0));
        }

        private static SingletonHolder getInstance() {
            return singleton.getSingleton();
        }
    }

    @Multiton(staticConstructor = "newInstance")
    public static class MultitonHolder {
        static MultitonMetacode<MultitonHolder> multiton = 
            TestMetaHelper.getMultiton(MultitonHolder.class);

        private static List<String> instances = new ArrayList<>();
        private String key;

        public static MultitonHolder getInstance(String key) {
            return multiton.getMultiton(key);
        }

        static MultitonHolder newInstance(Object obj) {
            String key = (String) obj;
            assertFalse(instances.contains(key));
            instances.add(key);
            MultitonHolder holder = new MultitonHolder();
            holder.key = key;
            return holder;
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
                    logger.debug("[%s] instance: %s, key: %s", Thread.currentThread().getName(), instance.toString(), instance.key);
                }
            });
            twoThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    MultitonHolder instance = MultitonHolder.getInstance("two");
                    assertNotNull(instance);
                    logger.debug("[%s] instance: %s, key: %s", Thread.currentThread().getName(), instance.toString(), instance.key);
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
