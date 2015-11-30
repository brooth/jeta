package org.javameta.samples.meta;

import org.javameta.base.Meta;
import org.javameta.samples.MetaHelper;
import org.javameta.util.Factory;
import org.javameta.util.Lazy;
import org.javameta.util.Provider;

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

        // todo: support? lazy?
        //Provider<SampleEntity> getSampleEntityProvider(String value);
    }

    public void testMeta() {
        MetaHelper.injectMeta(this);

        System.out.println(entity.getValue());
        System.out.println(entityClass.getSimpleName());
        System.out.println(sampleEntityLazy.get().getValue());
        System.out.println(sampleEntityProvider.get().getValue());

        System.out.println(factory.getSampleEntity("foo").getValue());
        //System.out.println(factory.getSampleEntityProvider("via provider").get().getValue());
    }

    public static void main(String[] args) {
        new MetaEntitySample().testMeta();
    }
}
