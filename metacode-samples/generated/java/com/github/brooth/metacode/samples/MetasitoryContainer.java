package com.github.brooth.metacode.samples;

import com.github.brooth.metacode.metasitory.HashMapMetasitoryContainer;
import java.lang.Class;
import java.lang.Override;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetasitoryContainer implements HashMapMetasitoryContainer {
  @Override
  public Map<Class, HashMapMetasitoryContainer.Context> get() {
    Map<Class, HashMapMetasitoryContainer.Context> result = new LinkedHashMap<>();
    result.put(com.github.brooth.metacode.samples.observer.ObserverSample.AsyncRequest.class,
    	new HashMapMetasitoryContainer.Context(
    		com.github.brooth.metacode.samples.observer.ObserverSample.AsyncRequest.class,
    		new javax.inject.Provider<com.github.brooth.metacode.samples.observer.ObserverSample_AsyncRequest_Metacode>() {
    			public com.github.brooth.metacode.samples.observer.ObserverSample_AsyncRequest_Metacode get() {
    				return new com.github.brooth.metacode.samples.observer.ObserverSample_AsyncRequest_Metacode();
    		}},
    		new Class[] {
    			com.github.brooth.metacode.observer.Subject.class
    		}));
    result.put(com.github.brooth.metacode.samples.observer.ObserverSample.RequestWindow.class,
    	new HashMapMetasitoryContainer.Context(
    		com.github.brooth.metacode.samples.observer.ObserverSample.RequestWindow.class,
    		new javax.inject.Provider<com.github.brooth.metacode.samples.observer.ObserverSample_RequestWindow_Metacode>() {
    			public com.github.brooth.metacode.samples.observer.ObserverSample_RequestWindow_Metacode get() {
    				return new com.github.brooth.metacode.samples.observer.ObserverSample_RequestWindow_Metacode();
    		}},
    		new Class[] {
    			com.github.brooth.metacode.observer.Observer.class
    		}));
    return result;
  }
}
