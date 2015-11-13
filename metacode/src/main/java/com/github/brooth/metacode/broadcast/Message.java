package com.github.brooth.metacode.broadcast;

import javax.annotation.Nullable;

/**
 *  
 */
public interface Message {
	
	int getId();

	@Nullable
	String getTag();
}
