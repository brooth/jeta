package org.javameta.validate;

import org.javameta.MasterMetacode;

import java.util.List;

/**
 *
 */
public interface ValidatorMetacode<M> extends MasterMetacode<M> {
    List<String> applyValidation(M master);
}
