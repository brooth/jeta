package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterController;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ObservableController<M> extends MasterController<M, ObservableController.ObservableMetacode<M>> {

    public ObservableController(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public void createObservable() {
        for (ObservableController.ObservableMetacode<M> observable : metacodes)
            observable.applyObservable(master);
    }

    public interface ObservableMetacode<M> extends MasterMetacode<M> {
        void applyObservable(M master);
    }
}
