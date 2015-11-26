package org.javameta.samples.observer;

import org.javameta.observer.Observer;
import org.javameta.observer.ObserverHandler;
import org.javameta.observer.Observers;
import org.javameta.observer.Subject;
import org.javameta.samples.MetaHelper;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
            handler.unregisterAll();
        }
    }

    public static void main(String[] args) {
        RequestWindow window = new RequestWindow();
        window.doRequest();
    }
}
