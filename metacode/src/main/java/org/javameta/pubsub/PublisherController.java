package org.javameta.pubsub;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;

/**
 * 
 */
public class PublisherController<M> extends MasterController<M, PublisherMetacode<M>> {

    public PublisherController(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public void createPublisher() {
        for (PublisherMetacode<M> observable : metacodes)
            observable.applyPublisher(master);
    }
}
