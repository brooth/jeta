package com.github.brooth.metacode.pubsub;

import com.github.brooth.metacode.MasterMetacode;

/**
* @author khalidov
* @version $Id$
*/
public interface SubscriberMetacode<M> extends MasterMetacode<M> {
    public SubscriptionHandler applySubscribers(M master);
}
