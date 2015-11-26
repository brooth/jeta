package org.javameta.util;

import org.javameta.MasterMetacode;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface SingletonMetacode<M> extends MasterMetacode<M> {
    M getInstance();
}
