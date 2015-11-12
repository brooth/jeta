package com.github.brooth.metacode.observer;


// same method for Broadcast/Reciever as global observer with filters

public class AsyncRequest extends Thread {

	@Observable
	protected Observers<CompleteEvent> observers;

	public AsyncRequest() {
		MetaHelper.createObservable(this);
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
		AsyncTask task = new AsyncRequest();
		MetaHelper.registerObserver(task, this);
		start();
	}

	@Observer(AsyncRequest.class)
	protected void onCompleteEvent(CompleteEvent e) {
		// ...
	}
}

// ------------------------------------------------------------

public final class AsyncRequest_Metacode {

	private final static Map<AsyncRequest, Observers<CompleteEvent>> observers = new WeakHashMap<>();

	public static Map<> getCompleteEventObservers(Object master) {
	   	if(observers.containsKey(master)
	   		observers.put(master, new Observers<>();
	   	return observers.get(master);
	}

	public void applyObservable(AsyncRequest master) {
		master.observers = getCompleteEventObservers(master);
	}
}

public final class RequestWindow_Metacode {

    public ObserverHandler applyObservers(Object master, Object observable) {
		ObserverHandler handler = new ObserverHandler();
		// handler.unregister(AsyncRequest.class, CompleteEvent.class);
		// handler.unregister(AsyncRequest.class);
		// handler.unregisterAll();
		if(observable.getClass() == AsyncRequest.class) {
			handler.add(AsyncRequest_Metacode.getCompleteEventObservers(observable)
				.register(new EventObserver() {
					@Override
         			void onEvent(CompleteEvent event) {
            			master.onCompleteEvent(event); 	
					}
				}));                                 
		}
		
		throw new IllegalStateException("not an observer of " + eventClass);
	}
}

