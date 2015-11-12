package com.github.brooth.metacode.broadcast;

/**
 *  
 */
@Broadcast(AlertMessage.class)
public class AlarmManager {

	public void alarm(String type, String msg) {
		MetaHelper.broadcast(getClass, new AlertMessage(type, msg);
	}

	public static class AlertMessage implements Message {

		private AlertMessage(String type, String msg) {}

		public getTag() { return type; }

		public getId() { return 42; }
	}
}

public class PanicAtTheDisco {

	private ReceiverHandler handler;

	public PanicAtTheDisco() {
		handler = MetaHelper.registerReceiver(this, AlarmManager.class);
	}

    @Receiver(AlarmManager.class, ids = 42)
	protected void alert(AlertMessage alert) {
     	handler.unregisterAll();
		quit();
	}
}
