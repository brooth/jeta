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

package org.brooth.jeta.samples.pubsub;

import org.brooth.jeta.pubsub.*;
import org.brooth.jeta.samples.MetaHelper;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class PublishSubscribeSample {

    static class Never implements Filter {
        @Override
        public boolean accepts(Object master, String methodName, Message msg) {
            return false;
        }
    }

    @MetaFilter(emitExpression = "$m.MIN_ALARM_ID <= $e.getId()")
    static interface MinAlarmIdFilter extends Filter { }

    /**
     * Publisher
     */
    public static class AlarmManager {
        @Publish
        protected Subscribers<AlertMessage> subscribers;

        public AlarmManager() {
            MetaHelper.createPublisher(this);
        }

        public void info(String msg) {
            subscribers.notify(new AlertMessage(1, msg));
        }

        public void alarm(String msg) {
            subscribers.notify(new AlertMessage(3, msg));
        }

        public static class AlertMessage extends BaseMessage {
            private AlertMessage(int level, String msg) {
                super(level, msg);
            }
        }
    }

    /**
     * Subscriber
     */
    public static class PanicAtTheDisco {
        final int MIN_ALARM_ID = 3;

        private SubscriptionHandler handler;

        public PanicAtTheDisco() {
            handler = MetaHelper.registerSubscriber(this);
        }

        @Subscribe(value = AlarmManager.class, ids = 1)
        protected void onInfo(AlarmManager.AlertMessage alert) {
            System.out.println("Info: '" + alert.getTopic() + "'");
        }

        @Subscribe(value = AlarmManager.class, filters = MinAlarmIdFilter.class)
        protected void onAlarm(AlarmManager.AlertMessage alert) {
            System.out.println("Error: '" + alert.getTopic() + "'. I quit!");
            quit();
        }

        @Subscribe(value = AlarmManager.class, filters = Never.class)
        protected void onApocalypse(AlarmManager.AlertMessage alert) {
            throw new IllegalStateException("Why God? Why?");
        }

        private void quit() {
            handler.unregisterAll();
        }
    }

    public static void main(String[] args) {
        new PanicAtTheDisco();
        AlarmManager alarmManager = new AlarmManager();
        alarmManager.info("Have a good day :)");
        alarmManager.alarm("The village is on fire!");
        alarmManager.alarm("Zombie!");
    }
}
