package com.github.brooth.metacode.samples.pubsub;

import com.github.brooth.metacode.pubsub.*;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class PublishSubscribeSample {

    static class NeverFilter implements Filter {
        @Override
        public boolean accepts(Object master, String methodName, Message msg) {
            return false;
        }
    }

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
        final String MIN_ALARM_FILTER = "%m.MIN_ALARM_ID <= %e.getId()";

        private SubscriptionHandler handler;

        public PanicAtTheDisco() {
            handler = MetaHelper.registerSubscriber(this);
        }

        @Subscribe(value = AlarmManager.class, ids = 1)
        protected void onInfo(AlarmManager.AlertMessage alert) {
            System.out.println("Info: '" + alert.getTopic() + "'");
        }

        @Subscribe(value = AlarmManager.class, filterExpression = MIN_ALARM_FILTER)
        protected void onAlarm(AlarmManager.AlertMessage alert) {
            System.err.println("Error: '" + alert.getTopic() + "'. I'm quitting!");
            quit();
        }

        @Subscribe(value = AlarmManager.class, filters = {NeverFilter.class})
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
        alarmManager.alarm("Zombies!");
    }
}
