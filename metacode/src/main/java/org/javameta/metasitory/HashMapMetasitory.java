package org.javameta.metasitory;

import org.javameta.MasterMetacode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Support ordering through containers. So, items from first container go first
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class HashMapMetasitory implements Metasitory {

    public static final int SUPPORTED_CRITERIA_VERSION = 1;

    private final List<Map<Class, MapMetasitoryContainer.Context>> containers = new CopyOnWriteArrayList<>();

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

        MapMetasitoryContainer container;
        try {
            container = (MapMetasitoryContainer) clazz.newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initiate class " + clazz, e);
        }

        containers.add(container.get());
    }

    @Override
    public List<MasterMetacode> search(Criteria criteria) {
        if (Criteria.VERSION > SUPPORTED_CRITERIA_VERSION)
            throw new IllegalArgumentException("Criteria version " + Criteria.VERSION + " not supported");

        List<MasterMetacode> result = new ArrayList<>();
        for (Map<Class, MapMetasitoryContainer.Context> container : containers) {
            // todo support criteria search
            MapMetasitoryContainer.Context context = container.get(criteria.getMasterAssignableTo());
            if (context != null)
                result.add(context.metacodeProvider.get());
        }

        return result;
    }
}

