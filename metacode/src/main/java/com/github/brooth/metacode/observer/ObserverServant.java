package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ObserverServant<M> extends MasterServant<M, ObserverServant.ObserverMetacode<M>> {

    public ObserverServant(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public ObserverHandler registerObserver(Object observable) {
        ObserverHandler handler = new ObserverHandler();
        for (ObserverServant.ObserverMetacode<M> observer : metacodes)
            handler.add(observer.applyObservers(master, observable));

        return handler;
    }

    public interface ObserverMetacode<M> extends MasterMetacode<M> {
        public ObserverHandler applyObservers(M master, Object observable);
    }
}
