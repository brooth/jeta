/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta.samples.observer;

import org.brooth.jeta.observer.Observer;
import org.brooth.jeta.observer.ObserverHandler;
import org.brooth.jeta.observer.Observers;
import org.brooth.jeta.observer.Subject;
import org.brooth.jeta.samples.MetaHelper;

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
            observers.notifyAndClear(new CompleteEvent("Jeta rules!"));
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
