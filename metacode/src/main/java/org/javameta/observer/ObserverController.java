package org.javameta.observer;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;

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
