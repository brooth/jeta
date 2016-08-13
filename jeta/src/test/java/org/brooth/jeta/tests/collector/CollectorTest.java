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

package org.brooth.jeta.tests.collector;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.MetaHelper;
import org.brooth.jeta.Provider;
import org.brooth.jeta.collector.ObjectCollector;
import org.brooth.jeta.collector.TypeCollector;
import org.brooth.jeta.log.Log;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class CollectorTest extends BaseTest {

    @Log
    Logger logger;

    @TypeCollector({UsedAnnotation.class, NoneAnnotation.class})
    public static class TypeCollectorHolder {
    }

    @ObjectCollector({UsedAnnotation.class, NoneAnnotation.class})
    public static class ObjectCollectorHolder {
    }

    @Test
    public void testUsedAnnotation() {
        logger.debug("testUsedAnnotation()");

        List<Class<?>>  types = MetaHelper.collectTypes(TypeCollectorHolder.class, UsedAnnotation.class);
        assertThat(types, allOf(notNullValue(), hasSize(6)));

        for(Class<?> type : types) {
            logger.debug("type: %s", type.getCanonicalName());
        }

        List<Provider<?>> objects = MetaHelper.collectObjects(ObjectCollectorHolder.class, UsedAnnotation.class);
        assertThat(objects, allOf(notNullValue(), hasSize(3)));

        for(Provider<?> provider : objects) {
            logger.debug("object: %s", provider.get().toString());
        }
    }

    @Test
    public void testNoneAnnotation() {
        logger.debug("testNoneAnnotation()");

        List<Class<?>>  types = MetaHelper.collectTypes(TypeCollectorHolder.class, NoneAnnotation.class);
        assertThat(types, allOf(notNullValue(), hasSize(0)));

        types = MetaHelper.collectTypes(TypeCollectorHolder.class, Log.class);
        assertThat(types, allOf(notNullValue(), hasSize(0)));

        List<Provider<?>> objects = MetaHelper.collectObjects(ObjectCollectorHolder.class, NoneAnnotation.class);
        assertThat(objects, allOf(notNullValue(), hasSize(0)));

        types = MetaHelper.collectTypes(TypeCollectorHolder.class, NoneAnnotation.class);
        assertThat(types, allOf(notNullValue(), hasSize(0)));

        objects = MetaHelper.collectObjects(ObjectCollectorHolder.class, Override.class);
        assertThat(objects, allOf(notNullValue(), hasSize(0)));
    }
}
