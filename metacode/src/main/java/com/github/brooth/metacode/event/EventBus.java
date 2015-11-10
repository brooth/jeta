package com.github.brooth.metacode.event;

import com.github.brooth.metacode.metasitory.Metasitory;

/**
 *  
 */
public class EventBus {

	public EventBus(Metasitory metasitory) {
		// search handlers
	}

	public void subscribe(Object master) {}

	public void unsubscribe(Object master) {}

	public void publish(Object event) {}

	public Thread publishAsync(Object event) {
		return null;
	}
}
