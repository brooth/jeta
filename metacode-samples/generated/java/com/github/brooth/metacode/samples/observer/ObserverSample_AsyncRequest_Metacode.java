package com.github.brooth.metacode.samples.observer;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.observer.ObservableServant;
import com.github.brooth.metacode.observer.Observers;
import java.lang.Class;
import java.util.Map;
import java.util.WeakHashMap;

public final class ObserverSample_AsyncRequest_Metacode implements MasterMetacode<ObserverSample.AsyncRequest>, ObservableServant.ObservableMetacode<ObserverSample.AsyncRequest> {
  private static Map<ObserverSample.AsyncRequest, Observers<ObserverSample.AsyncRequest.CompleteEvent>> observers = new WeakHashMap<>();

  public Class<ObserverSample.AsyncRequest> getMasterClass() {
    return ObserverSample.AsyncRequest.class;
  }

  /**
   * hash of "com.github.brooth.metacode.samples.observer.ObserverSample.AsyncRequest.CompleteEvent"
   */
  public static Observers<ObserverSample.AsyncRequest.CompleteEvent> getObserversN497218703(ObserverSample.AsyncRequest master) {
    Observers<ObserverSample.AsyncRequest.CompleteEvent> result = observers.get(master);
    if (result == null) {
      result = new Observers<ObserverSample.AsyncRequest.CompleteEvent>();
      observers.put(master, result);
    }
    return result;
  }

  public void applyObservable(ObserverSample.AsyncRequest master) {
    master.observers = getObserversN497218703(master);
  }
}
