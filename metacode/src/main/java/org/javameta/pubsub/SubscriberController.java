package org.javameta.pubsub;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;

/**
 * 
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
