package org.javameta.pubsub;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface PublisherMetacode<M> extends MasterMetacode<M> {
    void applyPublisher(M master);
}
