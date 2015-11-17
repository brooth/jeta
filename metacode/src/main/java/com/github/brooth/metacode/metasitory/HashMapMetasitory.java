package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.util.MultitonServant;
import com.github.brooth.metacode.util.Multiton;

import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapMetasitory implements Metasitory {

    private static final Map<String, HashMapMetasitory> multiton = new HashMap<>();

    private final Map<Class, HashMapMetasitoryContainer.Context> map;

    public static HashMapMetasitory getInstance(String metaPackage) {
        HashMapMetasitory instance = multiton.get(metaPackage);
        if (instance == null) {
            synchronized (HashMapMetasitory.class) {
                if (!multiton.containsKey(metaPackage)) {
                    instance = new HashMapMetasitory(metaPackage);
                    multiton.put(metaPackage, instance);
                }
            }
        }

        return instance;
    }

    private HashMapMetasitory(String metaPackage) {
        String className = metaPackage.isEmpty() ? "MetasitoryContainer" : metaPackage + ".MetasitoryContainer";
        Class<?> clazz;
        try {
            clazz = Class.forName(className);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class " + className, e);
        }

        HashMapMetasitoryContainer container;
        try {
            container = (HashMapMetasitoryContainer) clazz.newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initiate class " + clazz, e);
        }

        map = container.get();
    }

    @Override
    public List<MasterMetacode> search(Criteria criteria) {
        HashMapMetasitoryContainer.Context context = map.get(criteria.getMasterEqDeep());
        if (context == null)
            return Collections.emptyList();

        return Collections.singletonList(context.metacodeProvider.get());
    }
}

