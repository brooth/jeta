package org.javameta.pubsub;

import org.javameta.MasterMetacode;

public interface PublisherMetacode<M> extends MasterMetacode<M> {
    void applyPublisher(M master);
}
