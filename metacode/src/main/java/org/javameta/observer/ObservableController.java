package org.javameta.observer;

import org.javameta.MasterController;
import org.javameta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
