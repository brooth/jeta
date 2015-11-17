package com.github.brooth.metacode.samples.observer;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.observer.ObserverHandler;
import com.github.brooth.metacode.observer.ObserverServant;
import com.github.brooth.metacode.observer.Observers;
import java.lang.Class;
import java.lang.Object;
import java.lang.Override;

public final class ObserverSample_RequestWindow_Metacode implements MasterMetacode<ObserverSample.RequestWindow>, ObserverServant.ObserverMetacode<ObserverSample.RequestWindow> {
  public Class<ObserverSample.RequestWindow> getMasterClass() {
    return ObserverSample.RequestWindow.class;
  }

  public ObserverHandler applyObservers(final ObserverSample.RequestWindow master, Object observable) {
    if (observable.getClass() == ObserverSample.AsyncRequest.class) {
      ObserverHandler handler = new ObserverHandler();
      // hash of "com.github.brooth.metacode.samples.observer.ObserverSample.AsyncRequest.CompleteEvent";
      handler.add(ObserverSample.AsyncRequest.class, ObserverSample.AsyncRequest.CompleteEvent.class,
          ObserverSample_AsyncRequest_Metacode.getObserversN497218703((ObserverSample.AsyncRequest) observable).
          register(new Observers.EventObserver<ObserverSample.AsyncRequest.CompleteEvent>() {
            @Override
            public void onEvent(ObserverSample.AsyncRequest.CompleteEvent event) {
              master.onCompleteEvent(event);
            }
          }));
      return handler;
    }
    throw new IllegalArgumentException("Not an observer of " + observable.getClass());
  }
}
