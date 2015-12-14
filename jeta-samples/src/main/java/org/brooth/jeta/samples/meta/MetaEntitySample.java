/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta.samples.meta;

import org.brooth.jeta.meta.Meta;
import org.brooth.jeta.samples.MetaHelper;
import org.brooth.jeta.util.Factory;
import org.brooth.jeta.util.Lazy;
import org.brooth.jeta.util.Provider;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaEntitySample {
    @Meta
    SampleEntity entity;
    @Meta
    Class<? extends SampleEntity> entityClass;
    @Meta
    Lazy<SampleEntity> sampleEntityLazy;
    @Meta
    Provider<SampleEntity> sampleEntityProvider;
    @Meta
    MetaFactory factory;

    @Factory
    interface MetaFactory {
        SampleEntity getSampleEntity(String value);

        Provider<SampleEntity> getSampleEntityProvider(String value);

        Lazy<SampleEntity> getSampleEntityLazy(String value);
    }

    public void testMeta() {
        MetaHelper.injectMeta(this);

        System.out.println(entity.getValue());
        System.out.println(entityClass.getSimpleName());
        System.out.println(sampleEntityLazy.get().getValue());
        System.out.println(sampleEntityProvider.get().getValue());

        System.out.println(factory.getSampleEntity("factory").getValue());
        System.out.println(factory.getSampleEntityProvider("provider").get().getValue());
        System.out.println(factory.getSampleEntityLazy("lazy").get().getValue());
    }

    public static void main(String[] args) {
        new MetaEntitySample().testMeta();
    }
}
