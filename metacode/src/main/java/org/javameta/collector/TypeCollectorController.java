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

package org.javameta.collector;

import org.javameta.MasterClassController;
import org.javameta.metasitory.Metasitory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class TypeCollectorController extends MasterClassController<Object, TypeCollectorMetacode> {

    public TypeCollectorController(Metasitory metasitory, Class<?> masterClass) {
        super(metasitory, masterClass);
    }

    public List<Class> getTypes(Class<? extends Annotation> withAnnotation) {
        List<Class> result = new ArrayList<>();

        for (TypeCollectorMetacode collector : metacodes) {
            result.addAll(collector.getTypeCollection(withAnnotation));
        }

        return result;
    }
}
