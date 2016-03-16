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
 *
 */

package org.brooth.jeta.inject;

import org.brooth.jeta.MasterClassController;
import org.brooth.jeta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class StaticMetaController extends MasterClassController<Object, InjectMetacode<Object>> {

    public StaticMetaController(Metasitory metasitory, Class masterClass) {
        super(metasitory, masterClass, Meta.class);
    }

    public void inject(MetaScope<?> scope) {
        for (InjectMetacode<Object> metacode : metacodes)
            metacode.applyStaticMeta(scope);
    }
}
