package org.javameta.collector;

import org.javameta.MasterClassController;
import org.javameta.metasitory.Metasitory;
import org.javameta.util.Provider;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
