package org.javameta.metasitory;

import org.javameta.MasterMetacode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HashMapMetasitory implements Metasitory {

    public static final int SUPPORTED_CRITERIA_VERSION = 1;

    private final Map<Class, HashMapMetasitoryContainer.Context> map = new LinkedHashMap<>();

    public HashMapMetasitory(String metaPackage) {
        loadContainer(metaPackage);
    }

    public void loadContainer(String metaPackage) {
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

        map.putAll(container.get());
    }

    @Override
    public List<MasterMetacode> search(Criteria criteria) {
        if (Criteria.VERSION > SUPPORTED_CRITERIA_VERSION)
            throw new IllegalArgumentException("Criteria version " + Criteria.VERSION + " not supported");

        HashMapMetasitoryContainer.Context context = map.get(criteria.getMasterAssignableTo());
        if (context == null)
            return Collections.emptyList();

        return Collections.singletonList(context.metacodeProvider.get());
    }
}

