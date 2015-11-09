package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;

import java.util.List;

/**
 *
 */
public interface Metasitory {
	List<MasterMetacode<?>> search(Criteria criteria);
}

