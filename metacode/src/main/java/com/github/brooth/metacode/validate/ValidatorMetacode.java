package com.github.brooth.metacode.validate;

import com.github.brooth.metacode.MasterMetacode;

import java.util.List;

/**
 *
 */
public interface ValidatorMetacode<M> extends MasterMetacode<M> {
    List<String> applyValidation(M master);
}
