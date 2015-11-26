package org.javameta.pubsub;

import javax.annotation.Nullable;

/**
 *  
 */
public interface Message {
	
	int getId();

	@Nullable
	String getTopic();
}
