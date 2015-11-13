package com.github.brooth.metacode.samples.observer;

import com.github.brooth.metacode.observer.Observable;
import com.github.brooth.metacode.observer.Observer;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.observer.Observers;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class ObserverSample {

    /**
     * Observable
     */
    public static class AsyncRequest extends Thread {
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
            private String msg;

            private CompleteEvent(String msg) {
                this.msg = msg;
            }

            public String getStatus() {
                return msg;
            }
        }
    }

    /**
     * Observer
     */
    public static class RequestWindow {
        private ObserverHandler handler;

        public void doRequest() {
            AsyncRequest request = new ObserverSample.AsyncRequest();
            handler = MetaHelper.registerObserver(this, request);
            request.start();
        }

        @Observer(AsyncRequest.class)
        protected void onCompleteEvent(AsyncRequest.CompleteEvent e) {
            // ...
            System.out.print("request complete with status: " + e.getStatus());
        }

        protected void close() {
            handler.unregisterAll();
        }
    }

    public static void main(String[] args) {
        new RequestWindow().doRequest();
    }
}

//public final class AsyncRequest_Metacode {
//
//	private final static Map<AsyncRequest, Observers<CompleteEvent>> observers = new WeakHashMap<>();
//
//	public static Map<> getCompleteEventObservers(Object master) {
//	   	if(observers.containsKey(master)
//	   		observers.put(master, new Observers<>();
//	   	return observers.get(master);
//	}
//
//	public void applyObservable(AsyncRequest master) {
//		master.observers = getCompleteEventObservers(master);
//	}
//}
//
//public final class RequestWindow_Metacode {
//
//    public ObserverHandler applyObservers(Object master, Object observable) {
//		ObserverHandler handler = new ObserverHandler();
//		// handler.unregister(AsyncRequest.class, CompleteEvent.class);
//		// handler.unregister(AsyncRequest.class);
//		// handler.unregisterAll();
//		if(observable.getClass() == AsyncRequest.class) {
//			handler.add(AsyncRequest_Metacode.getCompleteEventObservers(observable)
//				.register(new EventObserver() {
//					@Override
//         			void onEvent(CompleteEvent event) {
//            			master.onCompleteEvent(event);
//					}
//				}));
//		}
//
//		throw new IllegalStateException("not an observer of " + eventClass);
//	}
//}

