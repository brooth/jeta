package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;

/**
 * 
 */
public interface SingletonMetacode<M> extends MasterMetacode<M> {
    M getInstance();
}
