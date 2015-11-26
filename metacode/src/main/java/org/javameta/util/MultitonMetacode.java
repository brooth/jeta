package org.javameta.util;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface MultitonMetacode<M, K> extends MasterMetacode<M> {
    M getInstance(K key);
}
