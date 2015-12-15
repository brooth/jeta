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

package org.brooth.jeta.tests.observer;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.TestMetaHelper;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.observer.Observer;
import org.brooth.jeta.observer.ObserverHandler;
import org.brooth.jeta.observer.Observers;
import org.brooth.jeta.observer.Subject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ObserverTest extends BaseTest {

    @Log
    Logger logger;

    public static class ObservableHolder {
        @Subject
        Observers<EventOne> oneObservers;
        @Subject
        Observers<EventTwo> twoObservers;
    }

    public static class OtherObservableHolder {
        @Subject
        Observers<EventOne> oneObservers;
    }

    public static class ObserverHolder {
        @Log
        Logger logger;

        volatile int onEventOneInvokes;
        volatile EventOne lastEventOne;

        volatile int onEventTwoInvokes;
        volatile EventTwo lastEventTwo;

        volatile int onOtherEventOneInvokes;
        volatile EventOne lastOtherEventOne;

        public ObserverHolder() {
            TestMetaHelper.createLogger(this);
        }

        @Observer(ObservableHolder.class)
        void onEventOne(EventOne event) {
            logger.debug("onEventOne(value: %s)", event.value);
            onEventOneInvokes++;
            lastEventOne = event;
        }

        @Observer(ObservableHolder.class)
        void onEventTwo(EventTwo event) {
            logger.debug("onEventTwo(value: %s)", event.value);
            onEventTwoInvokes++;
            lastEventTwo = event;
        }

        @Observer(OtherObservableHolder.class)
        void onOtherEventOne(EventOne event) {
            logger.debug("onOtherEventOne(value: %s)", event.value);
            onOtherEventOneInvokes++;
            lastOtherEventOne = event;
        }
    }

    public static class OtherObserverHolder {
        @Log
        Logger logger;

        volatile int onOtherEventOneInvokes;
        volatile EventOne lastOtherEventOne;

        public OtherObserverHolder() {
            TestMetaHelper.createLogger(this);
        }

        @Observer(OtherObservableHolder.class)
        void onOtherEventOne(EventOne event) {
            logger.debug("onOtherEventOne(value: %s)", event.value);
            onOtherEventOneInvokes++;
            lastOtherEventOne = event;
        }
    }

    @Test
    public void testSimpleNotify() {
        logger.debug("testSimpleNotify()");
        ObservableHolder observable = new ObservableHolder();
        TestMetaHelper.createObservable(observable);

        observable.oneObservers.notify(new EventOne("none"));

        ObserverHolder observer = new ObserverHolder();
        observable.oneObservers.notify(new EventOne("none 2"));
        assertThat(observer.onEventOneInvokes, is(0));
        assertThat(observer.lastEventOne, nullValue());

        ObserverHandler handler = TestMetaHelper.registerObserver(observer, observable);
        observable.oneObservers.notify(new EventOne("catch it"));
        assertThat(observer.onEventOneInvokes, is(1));
        assertThat(observer.lastEventOne, not(nullValue()));
        assertThat(observer.lastEventOne.value, is("catch it"));
        assertThat(observer.onEventTwoInvokes, is(0));
        assertThat(observer.lastEventTwo, nullValue());
        assertThat(observer.onOtherEventOneInvokes, is(0));
        assertThat(observer.lastOtherEventOne, nullValue());

        observable.twoObservers.notify(new EventTwo("catch two"));
        assertThat(observer.onEventTwoInvokes, is(1));
        assertThat(observer.lastEventTwo, not(nullValue()));
        assertThat(observer.lastEventTwo.value, is("catch two"));

        observable.twoObservers.notify(new EventTwo("catch two 2"));
        observable.twoObservers.notify(new EventTwo("catch two 3"));
        observable.twoObservers.notify(new EventTwo("catch two 4"));
        assertThat(observer.onEventTwoInvokes, is(4));
        assertThat(observer.lastEventTwo.value, is("catch two 4"));

        OtherObservableHolder otherObservable = new OtherObservableHolder();
        TestMetaHelper.createObservable(otherObservable);
        otherObservable.oneObservers.notify(new EventOne("other none"));
        assertThat(observer.onOtherEventOneInvokes, is(0));
        assertThat(observer.lastOtherEventOne, nullValue());
        assertThat(observer.onEventOneInvokes, is(1));

        handler.add(TestMetaHelper.registerObserver(observer, otherObservable));
        otherObservable.oneObservers.notify(new EventOne("catch other"));
        assertThat(observer.onOtherEventOneInvokes, is(1));
        assertThat(observer.lastOtherEventOne, not(nullValue()));
        assertThat(observer.lastOtherEventOne.value, is("catch other"));
        assertThat(observer.onEventOneInvokes, is(1));

        // unregistered by event & observable
        assertThat(handler.unregister(EventOne.class, ObservableHolder.class), is(1));
        observable.oneObservers.notifyAndClear(new EventOne("none catch it"));
        assertThat(observer.onEventOneInvokes, is(1));

        // event two still works
        observable.twoObservers.notifyAndClear(new EventTwo("catch two 5"));
        assertThat(observer.onEventTwoInvokes, is(5));
        assertThat(observer.lastEventTwo.value, is("catch two 5"));

        // unregistered from observable
        observable.twoObservers.notify(new EventTwo("catch two 6"));
        assertThat(observer.onEventTwoInvokes, is(5));

        // unregister all
        assertThat(handler.unregisterAll(), is(1));
        otherObservable.oneObservers.notify(new EventOne("catch other 2"));
        assertThat(observer.lastOtherEventOne.value, is("catch other"));
        assertThat(observer.onOtherEventOneInvokes, is(1));

        OtherObserverHolder otherObserver = new OtherObserverHolder();
        handler.add(TestMetaHelper.registerObserver(otherObserver, otherObservable));
        otherObservable.oneObservers.notify(new EventOne("catch other 3"));
        assertThat(otherObserver.lastOtherEventOne.value, is("catch other 3"));
        assertThat(otherObserver.onOtherEventOneInvokes, is(1));

        // register back
        handler.add(TestMetaHelper.registerObserver(observer, observable));
        handler.add(TestMetaHelper.registerObserver(observer, otherObservable));
        otherObservable.oneObservers.notify(new EventOne("catch other 4"));
        assertThat(observer.lastOtherEventOne.value, is("catch other 4"));
        assertThat(observer.onOtherEventOneInvokes, is(2));
        assertThat(otherObserver.lastOtherEventOne.value, is("catch other 4"));
        assertThat(otherObserver.onOtherEventOneInvokes, is(2));
        observable.twoObservers.notify(new EventTwo("catch two 7"));
        assertThat(observer.onEventTwoInvokes, is(6));

        otherObservable.oneObservers.clear();
        assertThat(handler.unregisterAll(), is(2));

        // handler.unregister()
        handler = TestMetaHelper.registerObserver(observer, observable);
        observable.oneObservers.notify(new EventOne("catch it 2"));
        observable.twoObservers.notify(new EventTwo("catch two 8"));
        assertThat(observer.onEventOneInvokes, is(2));
        assertThat(observer.onEventTwoInvokes, is(7));

        handler.unregisterAll(ObservableHolder.class);
        observable.oneObservers.notify(new EventOne("catch it 3"));
        observable.twoObservers.notify(new EventTwo("catch two 9"));
        assertThat(observer.onEventOneInvokes, is(2));
        assertThat(observer.onEventTwoInvokes, is(7));
    }

    @Test
    public void testAsyncNotify() {
        logger.debug("testAsyncNotify()");

        final ObservableHolder observable = new ObservableHolder();
        TestMetaHelper.createObservable(observable);
        final OtherObservableHolder otherObservable = new OtherObservableHolder();
        TestMetaHelper.createObservable(otherObservable);

        ObserverHolder observer = new ObserverHolder();
        TestMetaHelper.registerObserver(observer, observable);
        TestMetaHelper.registerObserver(observer, otherObservable);
        OtherObserverHolder otherObserver = new OtherObserverHolder();
        TestMetaHelper.registerObserver(otherObserver, otherObservable);

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    observable.oneObservers.notify(new EventOne("catch async"));
                    otherObservable.oneObservers.notify(new EventOne("catch other async"));
                }
            });
        }

        for (Thread thread : threads)
            thread.start();

        sleepQuietly(500);

        assertThat(observer.onEventOneInvokes, is(10));
        assertThat(observer.lastEventOne, not(nullValue()));
        assertThat(observer.onOtherEventOneInvokes, is(10));
        assertThat(observer.lastOtherEventOne, not(nullValue()));
        assertThat(otherObserver.onOtherEventOneInvokes, is(10));
        assertThat(otherObserver.lastOtherEventOne, not(nullValue()));
    }

    public static class ConcurrentModificationTestHolder {
        @Log
        Logger logger;

        ObserverHandler handler;

        public ConcurrentModificationTestHolder(OtherObservableHolder observable) {
            TestMetaHelper.createLogger(this);
            handler = TestMetaHelper.registerObserver(this, observable);
        }

        @Observer(OtherObservableHolder.class)
        void onOtherEventOne(EventOne event) {
            logger.debug("onOtherEventOne()");
            // unregister during the event is being notified
            handler.unregisterAll();
        }
    }

    @Test
    public void testConcurrentModificationException() {
        logger.debug("testConcurrentModificationException()");

        OtherObservableHolder observable = new OtherObservableHolder();
        TestMetaHelper.createObservable(observable);
        ConcurrentModificationTestHolder holder = new ConcurrentModificationTestHolder(observable);
        observable.oneObservers.notify(new EventOne("boom"));
        assertThat(observable.oneObservers.getAll().size(), is(0));
        assertThat(holder.handler.unregisterAll(), is(0));
    }

}
