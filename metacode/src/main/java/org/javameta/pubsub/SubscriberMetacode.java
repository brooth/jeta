package org.javameta.pubsub;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface SubscriberMetacode<M> extends MasterMetacode<M> {
    public SubscriptionHandler applySubscribers(M master);
}
