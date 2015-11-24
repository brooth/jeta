package com.github.brooth.metacode.samples.pubsub;

import com.github.brooth.metacode.pubsub.*;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class PublishSubscribeSample {

    /**
     * Publisher
     */
    public static class AlarmManager {
        @Publish
        protected Subscribers<AlertMessage> subscribers;

        public AlarmManager() {
            MetaHelper.createPublisher(this);
        }

        public void alarm(String msg) {
            subscribers.notify(new AlertMessage(msg));
        }

        public static class AlertMessage extends BaseMessage {
            private AlertMessage(String msg) {
                super(msg);
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

        @Subscribe(AlarmManager.class)
        protected void alert(AlarmManager.AlertMessage alert) {
            handler.unregisterAll();
            quit(alert.getTopic());
        }

        private void quit(String reason) {
            System.err.println(reason + " I'm quitting!");
        }
    }

    public static void main(String[] args) {
        new PanicAtTheDisco();
        new AlarmManager().alarm("The village is on fire!");
    }
}