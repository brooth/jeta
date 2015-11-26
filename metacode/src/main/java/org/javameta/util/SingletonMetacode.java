package org.javameta.util;

import org.javameta.MasterMetacode;

/**
 * 
 */
public interface SingletonMetacode<M> extends MasterMetacode<M> {
    M getInstance();
}
