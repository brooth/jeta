package com.github.brooth.metacode.pubsub;

import com.github.brooth.metacode.MasterMetacode;

/**
 * 
 */
public interface SubscriberMetacode<M> extends MasterMetacode<M> {
    public SubscriptionHandler applySubscribers(M master);
}
