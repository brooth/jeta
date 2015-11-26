package org.javameta.observer;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface ObservableMetacode<M> extends MasterMetacode<M> {
    void applyObservable(M master);
}
