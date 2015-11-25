package com.github.brooth.metacode.collector;

import com.github.brooth.metacode.MasterClassController;
import com.github.brooth.metacode.metasitory.Metasitory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.github.brooth.metacode.util.Provider;

/**
 *
 */
public class ObjectCollectorController extends MasterClassController<Object, ObjectCollectorMetacode> {

    public ObjectCollectorController(Metasitory metasitory, Class<?> masterClass) {
        super(metasitory, masterClass);
    }

    public List<Provider<?>> getObjects(Class<? extends Annotation> withAnnotation) {
        List<Provider<?>> result = new ArrayList<>();

        for (ObjectCollectorMetacode collector : metacodes) {
            result.addAll(collector.getObjectCollection(withAnnotation));
        }

        return result;
    }
}
