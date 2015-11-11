package com.github.brooth.metacode.observer;

public class AsyncRequest extends Thread {
	protected Observers<CompleteEvent> observer;

	public AsyncRequest(Object observer) {
		observers = MetaHelper.createObservers<>();
		observers.add(observer);
	}

 	public void run() {
		// ...
		observers.notifyAndClear(new CompleteEvent("hello world"));
	}

	public static class CompleteEvent {
		private CompleteEvent(String msg) {}
	}
}

public class RequestWindow {
	public void doRequest() {
		new AsyncRequest(this).start();
	}

	@Observer
	protected void onCompleteEvent(CompleteEvent e) {
	}
}

public final class RequestWindow_Metacode {
	public <E> Observer<E> getObserver(M master, Class<E> eventClass) {
		if(eventClass == CompleteEvent.class)
			return new Observer<CompleteEvent>() {
         		void onEvent(CompleteEvent event) {
            		master.onCompleteEvent(event); 	
				}
			};
		
		throw new IllegalStateException("not an observer of " + eventClass);
	}
}

