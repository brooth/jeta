package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.util.MultitonServant;
import com.github.brooth.metacode.util.Multiton;

import java.util.List;

@Multiton
public class HashMapMetasitory implements Metasitory {

    private static final MultitonServant<HashMapMetasitory, String> multiton
            = new MultitonServant<>(HashMapMetasitory.class, String.class);

    public static HashMapMetasitory getInstance(String metaPackage) {
        return multiton.getInstance(metaPackage);
    }

    HashMapMetasitory(String metaPackage) {
        //...
    }

    @Override
    public List<MasterMetacode> search(Criteria criteria) {
        return null;
    }
}

