package com.github.brooth.metacode.pubsub;

import com.github.brooth.metacode.MasterMetacode;

public interface PublisherMetacode<M> extends MasterMetacode<M> {
    void applyPublisher(M master);
}
