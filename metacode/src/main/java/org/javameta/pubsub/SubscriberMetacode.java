package org.javameta.pubsub;

import org.javameta.MasterMetacode;

/**
 * 
 */
public interface SubscriberMetacode<M> extends MasterMetacode<M> {
    public SubscriptionHandler applySubscribers(M master);
}
