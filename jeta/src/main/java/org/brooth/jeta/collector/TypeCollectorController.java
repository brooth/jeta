/*
 * Copyright 2016 Oleg Khalidov
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

package org.brooth.jeta.collector;

import org.brooth.jeta.MasterClassController;
import org.brooth.jeta.metasitory.Metasitory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class TypeCollectorController extends MasterClassController<Object, TypeCollectorMetacode> {

    public TypeCollectorController(Metasitory metasitory, Class<?> masterClass) {
        super(metasitory, masterClass, TypeCollector.class);
    }

    public List<Class<?>>  getTypes(Class<? extends Annotation> annotation) {
        assert annotation!=null;

        List<Class<?>>  result = new ArrayList<Class<?>>();
        for (TypeCollectorMetacode collector : metacodes) {
            List<Class<?>>  collection = collector.getTypeCollection(annotation);
            if (collection != null)
                result.addAll(collection);
        }

        return result;
    }
}
