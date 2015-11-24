package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterMetacode;

/**
* @author khalidov
* @version $Id$
*/
public interface ObserverMetacode<M> extends MasterMetacode<M> {
    public ObserverHandler applyObservers(M master, Object observable);
}
