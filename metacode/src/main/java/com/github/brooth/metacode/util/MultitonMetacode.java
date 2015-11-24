package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;

/**
 * 
 */
public interface MultitonMetacode<M, K> extends MasterMetacode<M> {
    M getInstance(K key);
}
