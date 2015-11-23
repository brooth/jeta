package com.github.brooth.metacode.metasitory;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.util.MultitonController;
import com.github.brooth.metacode.util.Multiton;

import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class HashMapMetasitory implements Metasitory {

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
        HashMapMetasitoryContainer.Context context = map.get(criteria.getMasterEqDeep());
        if (context == null)
            return Collections.emptyList();

        return Collections.singletonList(context.metacodeProvider.get());
    }
}

