package com.github.brooth.metacode.observer;

import com.github.brooth.metacode.MasterMetacode;

public interface ObservableMetacode<M> extends MasterMetacode<M> {
    void applyObservable(M master);
}
