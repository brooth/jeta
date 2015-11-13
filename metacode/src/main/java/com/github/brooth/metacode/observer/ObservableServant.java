package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * @author khalidov
 * @version $Id$
 */
public class ObservableServant<M> extends MasterServant<M, ObservableServant.Observable<M>> {

    public ObservableServant(Metasitory metasitory, M master) {
        super(metasitory, master);
    }

    public void createObservable() {
        for (ObservableServant.Observable<M> observable : metacodes)
            observable.applyObservable(master);
    }

    public interface Observable<M> extends MasterMetacode<M> {
        void applyObservable(M master);
    }
}
