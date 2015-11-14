package com.github.brooth.metacode.observer;

/**
 *
 */
public final class ObserverHandler {

    public void add(Class observableClass, Class eventClass, Observers.Handler handler) {
	
	}

    public void add(ObserverHandler other) {

    }

    public boolean unregister(Class eventClass, Class observableClass) {
		return false;	
	}

    public boolean unregister(Class eventClass) {
        return false;
    }

    public void unregisterAll(Class observableClass) {
	
	}

    public void unregisterAll() {

    }
}
