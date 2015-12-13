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

package org.javameta.base;

import org.javameta.IMetacode;
import org.javameta.metasitory.Criteria;
import org.javameta.metasitory.Metasitory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaEntityFactory {

    protected Map<Class<?>, MetaEntityMetacode<?, ?>> graph;

    public MetaEntityFactory(Metasitory metasitory) {
        Collection<IMetacode<?>> entities = metasitory.search(new Criteria.Builder().usesAny(MetaEntity.class).build());
        this.graph = new HashMap<>(entities.size());

        // fill all meta items
        for (IMetacode<?> entity : entities) {
            if (entity instanceof MetaEntityMetacode) {
                MetaEntityMetacode<?, ?> impl = (MetaEntityMetacode<?, ?>) entity;
                MetaEntityMetacode<?, ?> existing = graph.get(impl.getMetaEntityOfClass());
                if (existing != null && existing.getMetaEntityPriority() < impl.getMetaEntityPriority())
                    continue;

                graph.put(impl.getMetaEntityOfClass(), impl);

            } else if (graph.get(entity.getMasterClass()) == null) {
                graph.put(entity.getMasterClass(), null);
            }
        }

        // look up extenders
        for (Class<?> entityClass : graph.keySet()) {
            MetaEntityMetacode<?, ?> impl = graph.get(entityClass);
            if (impl == null)
                continue;

            Class<?> extEntityClass = impl.getMetaEntityExtClass();
            while (extEntityClass != null) {
                if (!graph.containsKey(extEntityClass))
                    throw new IllegalStateException("Can't extend " + extEntityClass + ", not a meta entity");

                MetaEntityMetacode<?, ?> extImpl = graph.get(extEntityClass);
                Class<?> extImplExt = null;
                if (extImpl != null) {
                    // already extended?
                    if (impl == extImpl)
                        break;
                    // default provider (lowest priority) or already extended with higher priority
                    if (extEntityClass != extImpl.getMetaEntityOfClass() &&
                            extImpl.getMetaEntityPriority() > impl.getMetaEntityPriority())
                        break;
                    extImplExt = extImpl.getMetaEntityExtClass();
                }

                graph.put(extEntityClass, impl);
                extEntityClass = extImplExt;
            }
        }
    }

    public IMetaEntity getMetaEntity(Class<?> masterClass) {
        if (!isMetaEntity(masterClass))
            throw new IllegalArgumentException("'" + masterClass + "' is not a meta entity.");

        MetaEntityMetacode<?, ?> impl = graph.get(masterClass);
        if (impl == null)
            throw new IllegalStateException("Meta entity '" + masterClass + "' has no implementation.");

        return impl.getMetaEntityImpl();
    }


    public boolean isMetaEntity(Class<?> clazz) {
        return graph.containsKey(clazz);
    }

    public boolean hasMetaImplementation(Class<?> clazz) {
        return graph.get(clazz) != null;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getMetaEntityClass(Class<T> clazz) {
        MetaEntityMetacode<?, ?> impl = graph.get(clazz);
        if (impl == null)
            return clazz;

        return (Class<? extends T>) impl.getMetaEntityImpl().getEntityClass();
    }
}
