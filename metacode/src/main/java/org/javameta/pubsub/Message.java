package org.javameta.pubsub;

import javax.annotation.Nullable;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Message {
	
	int getId();

	@Nullable
	String getTopic();
}
