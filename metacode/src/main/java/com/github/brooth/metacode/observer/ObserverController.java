package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * 
 */
public class ObserverController<M> extends MasterController<M, ObserverMetacode<M>> {

    public ObserverController(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public ObserverHandler registerObserver(Object observable) {
        return registerObserver(observable, observable.getClass());
    }

    public ObserverHandler registerObserver(Object observable, Class observableClass) {   
        ObserverHandler handler = new ObserverHandler();
        for (ObserverMetacode<M> observer : metacodes)
            handler.add(observer.applyObservers(master, observable, observableClass));

        return handler;
    }
}
