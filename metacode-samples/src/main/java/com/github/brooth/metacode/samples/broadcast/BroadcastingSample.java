package com.github.brooth.metacode.samples.broadcast;

import com.github.brooth.metacode.broadcast.*;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class BroadcastingSample {

    /**
     * Broadcaster
     */
    @Broadcast(AlarmManager.AlertMessage.class)
    public static class AlarmManager {

        public void alarm(String type) {
            MetaHelper.broadcastMessage(getClass(), new AlertMessage(type));
        }

        public static class AlertMessage extends BaseMessage {
            public AlertMessage(String type) {
                super(42, type);
            }
        }
    }

    /**
     * Receiver
     */
    public static class PanicAtTheDisco {
        private ReceiverHandler handler;

        public PanicAtTheDisco() {
            handler = MetaHelper.registerReceiver(this, AlarmManager.class);
        }

        @Receiver(AlarmManager.class)
        protected void alert(AlarmManager.AlertMessage alert) {
            handler.unregisterAll();
            quit(alert.getTag());
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