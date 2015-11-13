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
    @Publisher(AlarmManager.AlertMessage.class)
    public static class AlarmManager {

        public void alarm(String type) {
            MetaHelper.publishMessage(AlarmManager.class, new AlertMessage(type));
        }

        public static class AlertMessage extends BaseMessage {
            private AlertMessage(String type) {
                super(42, type);
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