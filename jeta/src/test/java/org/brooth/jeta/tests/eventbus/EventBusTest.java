/*
 * Copyright 2016 Oleg Khalidov
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
 *
 */

package org.brooth.jeta.tests.eventbus;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.MetaHelper;
import org.brooth.jeta.eventbus.*;
import org.brooth.jeta.log.Log;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class EventBusTest extends BaseTest {

    @Log
    Logger logger;

    public static class SubscribeHolder {
        @Log
        Logger logger;

        volatile int onMessageOneInvokes = 0;
        volatile MessageOne lastMessageOne;

        volatile int onMessageTwoInvokes = 0;
        volatile MessageTwo lastMessageTwo;

        public SubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe
        void onMessageOne(MessageOne message) {
            logger.debug("onMessageOne(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneInvokes++;
            lastMessageOne = message;
        }

        @Subscribe
        void onMessageTwo(MessageTwo message) {
            logger.debug("onMessageTwo(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageTwoInvokes++;
            lastMessageTwo = message;
        }
    }

    @Test
    public void testSimpleNotify() {
        logger.debug("testSimpleNotify()");

        final SubscribeHolder subscriber = new SubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(subscriber);

        MetaHelper.getEventBus().publish(new MessageOne(1, "one"));
        assertThat(subscriber.onMessageOneInvokes, is(1));
        MatcherAssert.assertThat(subscriber.lastMessageOne.getTopic(), is("one"));
        MatcherAssert.assertThat(subscriber.lastMessageOne.getId(), is(1));

        handler.unregisterAll();
        MetaHelper.getEventBus().publish(new MessageOne(1, "none"));
        assertThat(subscriber.onMessageOneInvokes, is(1));
    }

    @Test
    public void testAsyncNotify() {
        logger.debug("testAsyncNotify()");

        SubscribeHolder subscriber = new SubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(subscriber);

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    MetaHelper.getEventBus().publish(new MessageOne(1, "one"));
                    MetaHelper.getEventBus().publish(new MessageTwo(2, "two"));
                }
            });
        }

        for (Thread thread : threads)
            thread.start();

        sleepQuietly(500);

        assertThat(subscriber.onMessageOneInvokes, is(10));
        assertThat(subscriber.onMessageTwoInvokes, is(10));
        assertThat(subscriber.lastMessageTwo.getId(), is(2));
        handler.unregisterAll();
    }

    public static class HighPrioritySubscribeHolder {
        @Log
        Logger logger;

        int onMessageOneInvokes = 0;

        public HighPrioritySubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe(priority = Integer.MAX_VALUE)
        void onMessageOne(MessageOne message) {
            logger.debug("onMessageOne(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneInvokes++;
            MatcherAssert.assertThat(message.getId(), is(1));
            message.incId();
        }
    }

    public static class LowPrioritySubscribeHolder {
        @Log
        Logger logger;

        int onMessageOneInvokes = 0;

        public LowPrioritySubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe(priority = Integer.MIN_VALUE)
        void onMessageOne(MessageOne message) {
            logger.debug("onMessageOne(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneInvokes++;
            MatcherAssert.assertThat(message.getId(), is(2));
        }
    }

    @Test
    public void testPriority() {
        logger.debug("testPriority()");

        LowPrioritySubscribeHolder low = new LowPrioritySubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(low);

        MetaHelper.getEventBus().publish(new MessageOne(2, "one"));
        assertThat(low.onMessageOneInvokes, is(1));

        HighPrioritySubscribeHolder high = new HighPrioritySubscribeHolder();
        handler.add(MetaHelper.registerSubscriber(high));
        MetaHelper.getEventBus().publish(new MessageOne(1, "two"));
        assertThat(high.onMessageOneInvokes, is(1));
        assertThat(low.onMessageOneInvokes, is(2));

        handler.unregisterAll();
    }

    public static class IdFilterSubscribeHolder {
        @Log
        Logger logger;

        int onMessageOneId1Invokes = 0;
        int onMessageOneId2Invokes = 0;

        public IdFilterSubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe(id = 1)
        void onMessageOneId1(MessageOne message) {
            logger.debug("onMessageOneId1(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneId1Invokes++;
            MatcherAssert.assertThat(message.getId(), is(1));
        }

        @Subscribe(id = {2, 4})
        void onMessageOneId2(MessageOne message) {
            logger.debug("onMessageOneId2(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneId2Invokes++;
            MatcherAssert.assertThat(message.getId(), anyOf(is(2), is(4)));
        }
    }

    @Test
    public void testIdFilter() {
        logger.debug("testIdFilter()");

        IdFilterSubscribeHolder subscriber = new IdFilterSubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(subscriber);

        MetaHelper.getEventBus().publish(new MessageOne(1, "one"));
        assertThat(subscriber.onMessageOneId1Invokes, is(1));
        assertThat(subscriber.onMessageOneId2Invokes, is(0));

        MetaHelper.getEventBus().publish(new MessageOne(2, "two"));
        assertThat(subscriber.onMessageOneId1Invokes, is(1));
        assertThat(subscriber.onMessageOneId2Invokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(3, "none"));
        assertThat(subscriber.onMessageOneId1Invokes, is(1));
        assertThat(subscriber.onMessageOneId2Invokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(4, "four"));
        assertThat(subscriber.onMessageOneId1Invokes, is(1));
        assertThat(subscriber.onMessageOneId2Invokes, is(2));

        handler.unregisterAll();
    }

    public static class TopicFilterSubscribeHolder {
        @Log
        Logger logger;

        int onMessageOneTopicOneInvokes = 0;
        int onMessageOneTopicTwoInvokes = 0;

        public TopicFilterSubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe(topic = "one")
        void onMessageOneTopicOne(MessageOne message) {
            logger.debug("onMessageOneTopicOne(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneTopicOneInvokes++;
            MatcherAssert.assertThat(message.getId(), is(1));
        }

        @Subscribe(topic = {"two", "four"})
        void onMessageOneTopicTwo(MessageOne message) {
            logger.debug("onMessageOneTopicTwo(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneTopicTwoInvokes++;
            MatcherAssert.assertThat(message.getId(), anyOf(is(2), is(4)));
        }
    }

    @Test
    public void testTopicFilter() {
        logger.debug("testTopicFilter()");

        TopicFilterSubscribeHolder subscriber = new TopicFilterSubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(subscriber);

        MetaHelper.getEventBus().publish(new MessageOne(1, "one"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(0));

        MetaHelper.getEventBus().publish(new MessageOne(2, "two"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(3, "none"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(4, "four"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(2));

        MetaHelper.getEventBus().publish(new MessageOne(5, "twofour"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(2));

        MetaHelper.getEventBus().publish(new MessageOne(6, "two "));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(2));

        MetaHelper.getEventBus().publish(new MessageOne(7, " four"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(2));

        MetaHelper.getEventBus().publish(new MessageOne(8, "tw.*"));
        assertThat(subscriber.onMessageOneTopicOneInvokes, is(1));
        assertThat(subscriber.onMessageOneTopicTwoInvokes, is(2));

        handler.unregisterAll();
    }

    public static class OddIdFilter implements Filter<Message> {
        public boolean accepts(Object master, String methodName, Message msg) {
            return msg.getId() % 2 != 0;
        }
    }

    public static class EvenIdFilter implements Filter<Message> {
        public boolean accepts(Object master, String methodName, Message msg) {
            return msg.getId() % 2 == 0;
        }
    }

    public static class CustomFilterSubscribeHolder {
        @Log
        Logger logger;

        int onMessageOneOddInvokes = 0;
        int onMessageOneEventInvokes = 0;

        public CustomFilterSubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe(filters = OddIdFilter.class)
        void onMessageOneOdd(MessageOne message) {
            logger.debug("onMessageOneOdd(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneOddInvokes++;
            MatcherAssert.assertThat(message.getId() % 2, is(not(0)));
        }

        @Subscribe(filters = EvenIdFilter.class)
        void onMessageOneEvent(MessageOne message) {
            logger.debug("onMessageOneEvent(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneEventInvokes++;
            MatcherAssert.assertThat(message.getId() % 2, is(0));
        }

        @Subscribe(filters = {OddIdFilter.class, EvenIdFilter.class})
        void onMessageOneNone(MessageOne message) {
            logger.debug("onMessageOneNone(id: %d, topic: %s)", message.getId(), message.getTopic());
            assertThat(true, is(false));
        }
    }

    @Test
    public void testCustomFilter() {
        logger.debug("testCustomFilter()");

        CustomFilterSubscribeHolder subscriber = new CustomFilterSubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(subscriber);

        MetaHelper.getEventBus().publish(new MessageOne(1, "one"));
        assertThat(subscriber.onMessageOneOddInvokes, is(1));
        assertThat(subscriber.onMessageOneEventInvokes, is(0));

        MetaHelper.getEventBus().publish(new MessageOne(2, "two"));
        assertThat(subscriber.onMessageOneOddInvokes, is(1));
        assertThat(subscriber.onMessageOneEventInvokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(3, "none"));
        assertThat(subscriber.onMessageOneOddInvokes, is(2));
        assertThat(subscriber.onMessageOneEventInvokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(4, "four"));
        assertThat(subscriber.onMessageOneOddInvokes, is(2));
        assertThat(subscriber.onMessageOneEventInvokes, is(2));

        handler.unregisterAll();
    }

    @MetaFilter(emitExpression = "$e.getId() % 2 != 0")
    public interface OddIdMetaFilter extends Filter {
    }

    @MetaFilter(emitExpression = "$e.getId() % 2 == 0")
    public interface EvenIdMetaFilter extends Filter {
    }

    public static class MetaFilterSubscribeHolder {
        @Log
        Logger logger;

        int onMessageOneOddInvokes = 0;
        int onMessageOneEventInvokes = 0;

        public MetaFilterSubscribeHolder() {
            MetaHelper.createLogger(this);
        }

        @Subscribe(filters = OddIdMetaFilter.class)
        void onMessageOneOdd(MessageOne message) {
            logger.debug("onMessageOneOdd(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneOddInvokes++;
            MatcherAssert.assertThat(message.getId() % 2, is(not(0)));
        }

        @Subscribe(filters = EvenIdMetaFilter.class)
        void onMessageOneEvent(MessageOne message) {
            logger.debug("onMessageOneEvent(id: %d, topic: %s)", message.getId(), message.getTopic());
            onMessageOneEventInvokes++;
            MatcherAssert.assertThat(message.getId() % 2, is(0));
        }

        @Subscribe(filters = {OddIdMetaFilter.class, EvenIdMetaFilter.class})
        void onMessageOneNone(MessageOne message) {
            logger.debug("onMessageOneNone(id: %d, topic: %s)", message.getId(), message.getTopic());
            assertThat(true, is(false));
        }
    }

    @Test
    public void testMetaFilter() {
        logger.debug("testMetaFilter()");

        MetaFilterSubscribeHolder subscriber = new MetaFilterSubscribeHolder();
        SubscriptionHandler handler = MetaHelper.registerSubscriber(subscriber);

        MetaHelper.getEventBus().publish(new MessageOne(1, "one"));
        assertThat(subscriber.onMessageOneOddInvokes, is(1));
        assertThat(subscriber.onMessageOneEventInvokes, is(0));

        MetaHelper.getEventBus().publish(new MessageOne(2, "two"));
        assertThat(subscriber.onMessageOneOddInvokes, is(1));
        assertThat(subscriber.onMessageOneEventInvokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(3, "none"));
        assertThat(subscriber.onMessageOneOddInvokes, is(2));
        assertThat(subscriber.onMessageOneEventInvokes, is(1));

        MetaHelper.getEventBus().publish(new MessageOne(4, "four"));
        assertThat(subscriber.onMessageOneOddInvokes, is(2));
        assertThat(subscriber.onMessageOneEventInvokes, is(2));

        handler.unregisterAll();
    }
}
