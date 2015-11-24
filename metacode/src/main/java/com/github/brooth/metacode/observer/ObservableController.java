package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * 
 */
public class ObservableController<M> extends MasterController<M, ObservableMetacode<M>> {

    public ObservableController(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public void createObservable() {
        for (ObservableMetacode<M> observable : metacodes)
            observable.applyObservable(master);
    }
}
