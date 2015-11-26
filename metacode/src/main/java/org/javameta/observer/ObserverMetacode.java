package org.javameta.observer;

import org.javameta.MasterMetacode;

/**
 * 
 */
public interface ObserverMetacode<M> extends MasterMetacode<M> {
    public ObserverHandler applyObservers(M master, Object observable, Class observableClass);
}
