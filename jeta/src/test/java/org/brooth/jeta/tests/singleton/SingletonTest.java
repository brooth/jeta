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

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.MetaHelper;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.util.Multiton;
import org.brooth.jeta.util.MultitonMetacode;
import org.brooth.jeta.util.Singleton;
import org.brooth.jeta.util.SingletonMetacode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class SingletonTest extends BaseTest {

    @Log
    Logger logger;

    @Singleton
    public static class SingletonHolder {
        static SingletonMetacode<SingletonHolder> singleton = 
            MetaHelper.getSingleton(SingletonHolder.class);

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
            MetaHelper.getMultiton(MultitonHolder.class);

        private static List<String> instances = new ArrayList<String>();
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
        final boolean[] successes = new boolean[threads.length];

        for (int i = 0; i < threads.length; i++) {
            final int finalI = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    SingletonHolder instance = SingletonHolder.getInstance();
                    assert instance != null;
                    logger.debug("[%s] instance: %s", Thread.currentThread().getName(), instance.toString());
                    successes[finalI] = true;
                }
            });
        }

        for (Thread thread : threads)
            thread.start();

        sleepQuietly(500);

        for (int i = 0; i < threads.length; i++) {
            assertTrue(successes[i]);
        }
    }

    @Test
    public void testMultiton() {
        logger.debug("testMultiton()");

        Thread[] oneThreads = new Thread[10];
        final boolean[] oneThreadSuccesses = new boolean[oneThreads.length];
        Thread[] twoThreads = new Thread[oneThreads.length];
        final boolean[] twoThreadSuccesses = new boolean[oneThreads.length];

        for (int i = 0; i < oneThreads.length; i++) {
            final int finalI = i;
            oneThreads[i] = new Thread(new Runnable() {
                public void run() {
                    MultitonHolder instance = MultitonHolder.getInstance("one");
                    assert instance != null;
                    logger.debug("[%s] instance: %s, key: %s", Thread.currentThread().getName(), instance.toString(), instance.key);
                    oneThreadSuccesses[finalI] = true;
                }
            });
            twoThreads[i] = new Thread(new Runnable() {
                public void run() {
                    MultitonHolder instance = MultitonHolder.getInstance("two");
                    assert instance != null;
                    logger.debug("[%s] instance: %s, key: %s", Thread.currentThread().getName(), instance.toString(), instance.key);
                    twoThreadSuccesses[finalI] = true;
                }
            });
        }

        for (int i = 0; i < oneThreads.length; i++) {
            oneThreads[i].start();
            twoThreads[i].start();
        }

        sleepQuietly(500);

        for (int i = 0; i < oneThreads.length; i++) {
            assertTrue(oneThreadSuccesses[i]);
            assertTrue(twoThreadSuccesses[i]);
        }
    }
}
