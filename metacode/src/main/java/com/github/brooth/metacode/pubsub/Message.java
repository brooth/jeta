package com.github.brooth.metacode.pubsub;

import javax.annotation.Nullable;

/**
 *  
 */
public interface Message {
	
	int getId();

	@Nullable
	String getTopic();
}
