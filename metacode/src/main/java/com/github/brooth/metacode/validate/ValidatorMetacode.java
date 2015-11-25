package com.github.brooth.metacode.validate;

import com.github.brooth.metacode.MasterMetacode;

import java.util.Set;

/**
 *
 */
public interface ValidatorMetacode<M> extends MasterMetacode<M> {
    Set<String> applyValidation(M master);
}
