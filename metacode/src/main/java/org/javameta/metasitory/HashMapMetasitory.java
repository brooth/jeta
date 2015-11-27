/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        // todo support criteria search
        List<MasterMetacode> result = new ArrayList<>();
        for (Map<Class, MapMetasitoryContainer.Context> container : containers) {
            if (criteria.getMasterEq() != null) {
                MapMetasitoryContainer.Context context = container.get(criteria.getMasterEq());
                result.add(context.metacodeProvider.get());
            }
            if (criteria.getMasterAssignableTo() != null) {
                MapMetasitoryContainer.Context context = container.get(criteria.getMasterAssignableTo());
                if (context != null) {
                    result.add(context.metacodeProvider.get());
                }
            }
        }

        return result;
    }
}

