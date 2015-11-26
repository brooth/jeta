package org.javameta.observer;

import org.javameta.MasterMetacode;

public interface ObservableMetacode<M> extends MasterMetacode<M> {
    void applyObservable(M master);
}
