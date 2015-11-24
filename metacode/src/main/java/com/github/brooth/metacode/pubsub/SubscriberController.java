package com.github.brooth.metacode.pubsub;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class SubscriberController<M> extends MasterController<M, SubscriberMetacode<M>> {

    public SubscriberController(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public SubscriptionHandler registerSubscriber() {
        SubscriptionHandler handler = new SubscriptionHandler();
        for (SubscriberMetacode<M> metacode : metacodes)
            handler.add(metacode.applySubscribers(master));

        return handler;
    }

}
