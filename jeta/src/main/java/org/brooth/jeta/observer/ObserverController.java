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

package org.brooth.jeta.observer;

import org.brooth.jeta.MasterController;
import org.brooth.jeta.metasitory.Metasitory;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ObserverController<M> extends MasterController<M, ObserverMetacode<M>> {

    public ObserverController(Metasitory metasitory, M master) {
        super(metasitory, master, Observe.class);
    }

    public ObserverHandler registerObserver(Object observable) {
        assert observable != null;
        return registerObserver(observable, observable.getClass());
    }

    public ObserverHandler registerObserver(Object observable, Class<?> observableClass) {
        assert observable != null;
        assert observableClass != null;

        ObserverHandler handler = new ObserverHandler();
        for (ObserverMetacode<M> observer : metacodes) {
            ObserverHandler observerHandler = observer.applyObservers(master, observable, observableClass);
            if (observerHandler == null)
                throw new IllegalArgumentException("Not an observer of " + observable.getClass());
            handler.add(observerHandler);
        }

        return handler;
    }
}
