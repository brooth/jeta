package org.javameta.util;

import org.javameta.MasterMetacode;

/**
 * 
 */
public interface MultitonMetacode<M, K> extends MasterMetacode<M> {
    M getInstance(K key);
}
