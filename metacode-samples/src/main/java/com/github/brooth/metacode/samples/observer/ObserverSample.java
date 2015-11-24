package com.github.brooth.metacode.samples.observer;

import com.github.brooth.metacode.observer.Observer;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.observer.Observers;
import com.github.brooth.metacode.observer.Subject;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class ObserverSample {

    /**
     * Observable
     */
    public static class AsyncRequest extends Thread {
        @Subject
        protected Observers<CompleteEvent> observers;

        public AsyncRequest() {
            MetaHelper.createObservable(this);
        }

        public void run() {
            // ...
            observers.notifyAndClear(new CompleteEvent("Hello world!"));
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
            System.out.print(String.format("request complete: [%s] %s",
                Thread.currentThread().getName(), e.getStatus()));
        }

        protected void close() {
			if(handler != null)
            	handler.unregisterAll();
        }
    }

    public static void main(String[] args) {
        RequestWindow window = new RequestWindow();
        window.doRequest();
        window.close();
    }
}
