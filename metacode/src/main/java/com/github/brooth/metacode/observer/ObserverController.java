package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ObserverController<M> extends MasterController<M, ObserverController.ObserverMetacode<M>> {

    public ObserverController(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public ObserverHandler registerObserver(Object observable) {
        ObserverHandler handler = new ObserverHandler();
        for (ObserverController.ObserverMetacode<M> observer : metacodes)
            handler.add(observer.applyObservers(master, observable));

        return handler;
    }

    public interface ObserverMetacode<M> extends MasterMetacode<M> {
        public ObserverHandler applyObservers(M master, Object observable);
    }
}
