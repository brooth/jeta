/*
 * Copyright 2016 Oleg Khalidov
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

import org.brooth.jeta.Metacode;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Support ordering through containers. So, items from first container go first
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MapMetasitory implements Metasitory {

    public static final int SUPPORTED_CRITERIA_VERSION = 1;

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    private Map<Class<?>, MapMetasitoryContainer.Context> meta;

    public MapMetasitory(String metaPackage) {
        loadContainer(metaPackage);
    }

    public MapMetasitory(MapMetasitoryContainer container) {
        loadContainer(container);
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

        loadContainer(container);
    }

    protected void loadContainer(MapMetasitoryContainer container) {
        try {
            writeLock.lock();

            if (meta == null)
                meta = container.get();
            else
                meta.putAll(container.get());

        } finally {
            writeLock.unlock();
        }
    }

    public void add(Metasitory other) {
        if (!(other instanceof MapMetasitory))
            throw new UnsupportedOperationException("Only other MapMetasitory is supported");
        try {
            writeLock.lock();
            meta.putAll(((MapMetasitory) other).meta);

        } finally {
            writeLock.unlock();
        }
    }

    public Collection<Metacode<?>> search(Criteria criteria) {
        if (Criteria.VERSION > SUPPORTED_CRITERIA_VERSION)
            throw new IllegalArgumentException("Criteria version " + Criteria.VERSION + " not supported");

        Map<Class<?>, MapMetasitoryContainer.Context> selection = meta;
        try {
            readLock.lock();
            selection = masterEq(selection, criteria);
            selection = masterEqDeep(selection, criteria);
            selection = usesAll(selection, criteria);
            selection = usesAny(selection, criteria);

        } finally {
            readLock.unlock();
        }

        if (selection.isEmpty())
            return Collections.emptyList();

        List<Metacode<?>> result = new ArrayList<>(selection.values().size());
        for (MapMetasitoryContainer.Context context : selection.values())
            result.add(context.metacodeProvider.get());
        return result;
    }

    private Map<Class<?>, MapMetasitoryContainer.Context> usesAny(Map<Class<?>, MapMetasitoryContainer.Context> selection, final Criteria criteria) {
        if (criteria.getUsesAny() == null)
            return selection;

        Map<Class<?>, MapMetasitoryContainer.Context> result = new HashMap<>();
        for (Map.Entry<Class<?>, MapMetasitoryContainer.Context> item : selection.entrySet()) {
            boolean add = false;
            for (Class<?> annotation : item.getValue().annotations) {
                for (Class<?> uses : criteria.getUsesAny()) {
                    if (annotation == uses) {
                        add = true;
                        break;
                    }
                }
                if (add)
                    break;
            }
            if (add)
                result.put(item.getKey(), item.getValue());
        }
        return result;
    }

    private Map<Class<?>, MapMetasitoryContainer.Context> usesAll(Map<Class<?>, MapMetasitoryContainer.Context> selection, final Criteria criteria) {
        if (criteria.getUsesAll() == null)
            return selection;
        if (criteria.getUsesAll().isEmpty())
            throw new IllegalArgumentException("criteria.useAll is empty");

        Map<Class<?>, MapMetasitoryContainer.Context> result = new HashMap<>();
        for (Map.Entry<Class<?>, MapMetasitoryContainer.Context> item : selection.entrySet()) {
            boolean add = true;
            for (Class<?> annotation : item.getValue().annotations) {
                boolean used = false;
                for (Class<?> uses : criteria.getUsesAll()) {
                    if (annotation == uses) {
                        used = true;
                        break;
                    }
                }
                if (!used) {
                    add = false;
                    break;
                }

            }
            if (add)
                result.put(item.getKey(), item.getValue());
        }
        return result;
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

