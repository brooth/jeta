package org.javameta.metasitory;

import org.javameta.MasterMetacode;

import java.util.Collection;

/**
 * Implementations should store version of criteria (@see Criteria.VERSION)
 * and throw IllegalArgumentException if not compatible with
 */
public interface Metasitory {
	Collection<MasterMetacode> search(Criteria criteria);
}

