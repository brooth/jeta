package com.github.brooth.metacode.pubsub;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
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
