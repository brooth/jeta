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

package org.brooth.jeta.metasitory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.brooth.jeta.IMetacode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Support ordering through containers. So, items from first container go first
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MapMetasitory implements Metasitory {

    public static final int SUPPORTED_CRITERIA_VERSION = 1;

    private Map<Class<?>, MapMetasitoryContainer.Context> meta;

    public MapMetasitory(String metaPackage) {
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

        if (meta == null)
            meta = container.get();
        else
            meta.putAll(container.get());
    }

    @SuppressWarnings("unused")
	@Override
    public Collection<IMetacode<?>> search(Criteria criteria) {
        if (Criteria.VERSION > SUPPORTED_CRITERIA_VERSION)
            throw new IllegalArgumentException("Criteria version " + Criteria.VERSION + " not supported");

        Map<Class<?>, MapMetasitoryContainer.Context> selection = meta;
        selection = masterEq(selection, criteria);
        selection = masterEqDeep(selection, criteria);
        selection = usesAll(selection, criteria);
        selection = usesAny(selection, criteria);

        if (selection.isEmpty())
            return Collections.emptyList();

        return Collections2.transform(selection.values(), new Function<MapMetasitoryContainer.Context, IMetacode<?>>() {
            @Override
            public IMetacode<?> apply(MapMetasitoryContainer.Context input) {
                return input.metacodeProvider.get();
            }
        });
    }

    private Map<Class<?>, MapMetasitoryContainer.Context> usesAny(Map<Class<?>, MapMetasitoryContainer.Context> selection, final Criteria criteria) {
        if (criteria.getUsesAny() == null)
            return selection;

        return Maps.filterValues(selection, new Predicate<MapMetasitoryContainer.Context>() {
            @Override
            public boolean apply(MapMetasitoryContainer.Context input) {
                for (Class<?> annotation : input.annotations) {
                    for (Class<?> uses : criteria.getUsesAny()) {
                        if (annotation == uses) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    private Map<Class<?>, MapMetasitoryContainer.Context> usesAll(Map<Class<?>, MapMetasitoryContainer.Context> selection, final Criteria criteria) {
        if (criteria.getUsesAll() == null)
            return selection;
        if (criteria.getUsesAll().isEmpty())
            throw new IllegalArgumentException("criteria.useAll is empty");

        return Maps.filterValues(selection, new Predicate<MapMetasitoryContainer.Context>() {
            @Override
            public boolean apply(MapMetasitoryContainer.Context input) {
                for (Class<?> need : criteria.getUsesAll()) {
                    boolean used = false;
                    for (Class<?> annotation : input.annotations) {
                        if (annotation == need) {
                            used = true;
                            break;
                        }
                    }
                    if (!used)
                        return false;
                }
                return true;
            }
        });
    }

    private Map<Class<?>, MapMetasitoryContainer.Context> masterEqDeep(Map<Class<?>, MapMetasitoryContainer.Context> selection, Criteria criteria) {
        if (criteria.getMasterEqDeep() == null)
            return selection;

        Map<Class<?>, MapMetasitoryContainer.Context> result = new HashMap<>();
        Class<?> clazz = criteria.getMasterEqDeep();
        while (clazz != Object.class) {
            MapMetasitoryContainer.Context context = selection.get(clazz);
            if (context != null)
                result.put(clazz, context);
            clazz = clazz.getSuperclass();
        }

        return result;
    }

    private Map<Class<?>, MapMetasitoryContainer.Context> masterEq(Map<Class<?>, MapMetasitoryContainer.Context> selection, Criteria criteria) {
        if (criteria.getMasterEq() == null)
            return selection;

        MapMetasitoryContainer.Context context = selection.get(criteria.getMasterEq());
        if (context == null)
            return Collections.emptyMap();

        return Collections.<Class<?>, MapMetasitoryContainer.Context>singletonMap(criteria.getMasterEq(), context);
    }
}

