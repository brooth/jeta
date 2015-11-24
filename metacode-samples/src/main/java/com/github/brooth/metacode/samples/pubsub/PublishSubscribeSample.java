package com.github.brooth.metacode.samples.pubsub;

import com.github.brooth.metacode.pubsub.*;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class PublishSubscribeSample {

    static class NoFilter implements Filter {
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
        private SubscriptionHandler handler;

        public PanicAtTheDisco() {
            handler = MetaHelper.registerSubscriber(this);
        }

        @Subscribe(value = AlarmManager.class, ids = 1)
        protected void onInfo(AlarmManager.AlertMessage alert) {
            System.out.println("Info: " + alert.getTopic());
        }

        @Subscribe(value = AlarmManager.class, ids = 3)
        protected void onAlarm(AlarmManager.AlertMessage alert) {
            quit(alert.getTopic());
        }

        @Subscribe(value = AlarmManager.class, filters = {NoFilter.class})
        protected void onApocalypse(AlarmManager.AlertMessage alert) {
            throw new IllegalStateException("Why god, why?");
        }

        private void quit(String reason) {
            System.err.print(reason + " I'm quitting!");
            handler.unregisterAll();
        }
    }

    public static void main(String[] args) {
        new PanicAtTheDisco();
        AlarmManager alarmManager = new AlarmManager();
        alarmManager.info("good day :)");
        alarmManager.alarm("The village is on fire!");
    }
}