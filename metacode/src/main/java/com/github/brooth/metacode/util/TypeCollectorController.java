package com.github.brooth.metacode.util;

import com.github.brooth.metacode.MasterClassController;
import com.github.brooth.metacode.metasitory.Metasitory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TypeCollectorController extends MasterClassController<Object, TypeCollectorMetacode> {

    protected TypeCollectorController(Metasitory metasitory, Class<?> masterClass) {
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
