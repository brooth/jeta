package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterMetacode;

/**
* @author khalidov
* @version $Id$
*/
public interface SingletonMetacode<M> extends MasterMetacode<M> {
    M getInstance();
}
