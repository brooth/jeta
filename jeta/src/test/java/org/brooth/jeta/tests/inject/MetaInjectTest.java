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

package org.brooth.jeta.tests.inject;

import org.brooth.jeta.*;
import org.brooth.jeta.inject.Inject;
import org.brooth.jeta.inject.MetaScope;
import org.brooth.jeta.inject.Producer;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.metasitory.ClassForNameMetasitory;
import org.brooth.jeta.metasitory.Criteria;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaInjectTest extends BaseTest {
    @Log
    Logger logger;

    @Producer
    public static class EntityOne {
        String value = "one";
    }

    public static class EntityHolder {
        @Inject
        EntityOne entity;
        @Inject
        Class<? extends EntityOne> clazz;
        @Inject
        Lazy<EntityOne> lazy;
        @Inject
        Provider<EntityOne> provider;
    }

    @Test
    public void testSimpleInject() {
        logger.debug("testSimpleInject()");

        EntityHolder holder = new EntityHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, is("one"));

        assertThat(holder.clazz, notNullValue());
        assertEquals(holder.clazz, EntityOne.class);

        assertThat(holder.lazy, notNullValue());
        assertFalse(holder.lazy.isPresent());
        assertThat(holder.lazy.get().value, is("one"));
        assertTrue(holder.lazy.isPresent());

        assertThat(holder.provider, notNullValue());
        assertThat(holder.provider.get().value, is("one"));

        assertTrue(holder.lazy.get() == holder.lazy.get());
        assertFalse(holder.provider.get() == holder.provider.get());

        assertFalse(holder.entity == holder.lazy.get());
        assertFalse(holder.entity == holder.provider.get());
        assertFalse(holder.lazy.get() == holder.provider.get());

        assertTrue(holder.lazy.get() == holder.lazy.release());
        assertFalse(holder.lazy.isPresent());
    }

    public static class StaticEntityHolder {
        @Inject
        static EntityOne entity;
        @javax.inject.Inject
        static EntityOne aliasEntity;
        @Inject
        static Class<? extends EntityOne> clazz;
        @Inject
        static Lazy<EntityOne> lazy;
        @Inject
        static Provider<EntityOne> provider;
    }

    @Test
    public void testStaticInject() {
        logger.debug("testStaticInject()");

        MetaHelper.injectStaticMeta(StaticEntityHolder.class);
        assertThat(StaticEntityHolder.entity, notNullValue());
        assertThat(StaticEntityHolder.entity.value, is("one"));

        assertThat(StaticEntityHolder.aliasEntity, notNullValue());
        assertThat(StaticEntityHolder.aliasEntity.value, is("one"));

        assertThat(StaticEntityHolder.clazz, notNullValue());
        assertEquals(StaticEntityHolder.clazz, EntityOne.class);

        assertThat(StaticEntityHolder.lazy, notNullValue());
        assertThat(StaticEntityHolder.lazy.get().value, is("one"));

        assertThat(StaticEntityHolder.provider, notNullValue());
        assertThat(StaticEntityHolder.provider.get().value, is("one"));

        assertTrue(StaticEntityHolder.lazy.get() == StaticEntityHolder.lazy.get());
        assertFalse(StaticEntityHolder.provider.get() == StaticEntityHolder.provider.get());

        assertFalse(StaticEntityHolder.entity == StaticEntityHolder.lazy.get());
        assertFalse(StaticEntityHolder.entity == StaticEntityHolder.provider.get());
        assertFalse(StaticEntityHolder.lazy.get() == StaticEntityHolder.provider.get());
    }

    @Producer(singleton = true)
    public static class SingletonEntity {
        String value = "default";

        public SingletonEntity() {
        }

        public SingletonEntity(String value) {
            this.value = value;
        }
    }

    @Producer(of = MetaInjectTest.SingletonEntity.class, singleton = true, scope = CustomScope.class)
    public static class SingletonEntityCustomScopeProvider {
        @Constructor
        public static SingletonEntity get() {
            return new SingletonEntity("provider");
        }
    }

    public static class SingletonHolder {
        @Inject
        SingletonEntity entity;
    }

    public static class SingletonHolder2 {
        @Inject
        SingletonEntity entity;
    }

    @Test
    public void testSingleton() {
        logger.debug("testSingleton()");

        SingletonHolder holder = new SingletonHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, notNullValue());
        assertThat(holder.entity.value, is("default"));

        SingletonEntity entityOne = holder.entity;
        MetaHelper.injectMeta(holder);
        assertTrue(entityOne == holder.entity);

        MetaScope<CustomScope> customMetaScope = MetaHelper.getMetaScope(new CustomScope());
        MetaHelper.injectMeta(customMetaScope, holder);
        assertTrue(entityOne != holder.entity);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, notNullValue());
        assertThat(holder.entity.value, is("provider"));

        entityOne = holder.entity;
        MetaHelper.injectMeta(customMetaScope, holder);
        assertTrue(entityOne == holder.entity);

        SingletonHolder2 holder2 = new SingletonHolder2();
        MetaHelper.injectMeta(customMetaScope, holder2);
        assertTrue(holder.entity == holder2.entity);

        customMetaScope = MetaHelper.getMetaScope(new CustomScope());
        MetaHelper.injectMeta(customMetaScope, holder2);
        assertTrue(holder.entity != holder2.entity);
    }

    public static class MetaAliasEntityHolder {
        @javax.inject.Inject
        EntityOne entity;
        @javax.inject.Inject
        Class<? extends EntityOne> clazz;
        @javax.inject.Inject
        Lazy<EntityOne> lazy;
        @javax.inject.Inject
        Provider<EntityOne> provider;
        @javax.inject.Inject
        javax.inject.Provider<EntityOne> javaxProvider;
    }

    @Test
    public void testAliasInject() {
        logger.debug("testAliasInject()");

        MetaAliasEntityHolder holder = new MetaAliasEntityHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, is("one"));

        assertThat(holder.clazz, notNullValue());
        assertEquals(holder.clazz, EntityOne.class);

        assertThat(holder.lazy, notNullValue());
        assertThat(holder.lazy.get().value, is("one"));

        assertThat(holder.provider, notNullValue());
        assertThat(holder.provider.get().value, is("one"));

        assertTrue(holder.lazy.get() == holder.lazy.get());
        assertFalse(holder.provider.get() == holder.provider.get());

        assertFalse(holder.entity == holder.lazy.get());
        assertFalse(holder.entity == holder.provider.get());
        assertFalse(holder.lazy.get() == holder.provider.get());

        assertThat(holder.javaxProvider, notNullValue());
        assertThat(holder.javaxProvider.get().value, is("one"));
    }

    public static class EntityTwo {
        String value;
    }

    @Producer(of = EntityTwo.class)
    public static class EntityTwoProvider {
        @Constructor
        public EntityTwo get() {
            EntityTwo e = new EntityTwo();
            e.value = "two";
            return e;
        }
    }

    public static class EntityThree {
        String value;
    }

    @Producer(of = EntityThree.class)
    public static class EntityThreeProvider {
        @Constructor
        public static EntityThree get() {
            EntityThree e = new EntityThree();
            e.value = "three";
            return e;
        }
    }

    public static class EntityFour {
        String value;
    }

    @Producer(of = EntityFour.class, staticConstructor = "getInstance")
    public static class EntityFourProvider {

        public static EntityFourProvider getInstance() {
            return new EntityFourProvider();
        }

        @Constructor
        public EntityFour get() {
            EntityFour e = new EntityFour();
            e.value = "four";
            return e;
        }
    }

    public static class MetaProviderHolder {
        @Inject
        EntityTwo entityTwo;
        @Inject
        EntityThree entityThree;
        @Inject
        EntityFour entityFour;
    }

    @Test
    public void testMetaProvider() {
        logger.debug("testMetaProvider()");

        MetaProviderHolder holder = new MetaProviderHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entityTwo, notNullValue());
        assertThat(holder.entityTwo.value, is("two"));
        logger.debug("entityTwo.value: %s", holder.entityTwo.value);

        assertThat(holder.entityThree, notNullValue());
        assertThat(holder.entityThree.value, is("three"));
        logger.debug("entityThree.value: %s", holder.entityThree.value);

        assertThat(holder.entityFour, notNullValue());
        assertThat(holder.entityFour.value, is("four"));
        logger.debug("entityFour.value: %s", holder.entityFour.value);
    }

    @Producer
    public static class EntityFive {
        String value;

        public EntityFive(String value) {
            this.value = value;
        }
    }

    public static class MetaFactoryHolder {
        @Inject
        MetaFactory factory;

        @Factory
        public interface MetaFactory {
            EntityFive get(String value);

            Class<? extends EntityFive> getClazz();

            Lazy<EntityFive> getLazy(String value);

            Provider<EntityFive> getProvider(String value);
        }

        @Factory
        public interface SuperSuperFactory {
            EntityFive getSuperSuper(String val);
        }

        @Factory
        public interface SuperFactory extends SuperSuperFactory {
            EntityFive getSuper(String val);
        }

        @Factory
        public interface ChildFactory extends SuperFactory {
            EntityFive getChild(String val);
        }

        @Inject
        ChildFactory childFactory;
    }

    @Test
    public void testMetaFactory() {
        logger.debug("testMetaFactory()");

        MetaFactoryHolder holder = new MetaFactoryHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.factory, notNullValue());
        assertThat(holder.factory.get(null), notNullValue());
        assertThat(holder.factory.get("five").value, is("five"));

        assertThat(holder.factory.getClazz(), notNullValue());
        assertEquals(holder.factory.getClazz(), EntityFive.class);

        assertThat(holder.factory.getLazy(null), notNullValue());
        assertThat(holder.factory.getLazy("lazyFive").get().value, is("lazyFive"));

        assertThat(holder.factory.getProvider(null), notNullValue());
        assertThat(holder.factory.getProvider("fiveProvider").get().value, is("fiveProvider"));

        assertThat(holder.childFactory, notNullValue());
        assertThat(holder.childFactory.getSuperSuper(null), notNullValue());
        assertThat(holder.childFactory.getSuper(null), notNullValue());
        assertThat(holder.childFactory.getChild(null), notNullValue());
    }

    @Producer
    public static class MetaDefaultScopeEntity {
        String value = "scope";
        String scopeData = null;

        public MetaDefaultScopeEntity(Object __scope__) {
            scopeData = ((DefaultScope) __scope__).getData();
        }

        public MetaDefaultScopeEntity(ExtScope scope) {
            assertTrue(false);
        }

        public MetaDefaultScopeEntity(String value, Object __scope__, boolean fake) {
            this.value = value;
            this.scopeData = ((DefaultScope) __scope__).getData();
        }
    }

    @Producer(scope = ExtScope.class)
    public static class MetaExtScopeEntity {
        String value = "ext scope";
        String scopeData = null;

        public MetaExtScopeEntity(Object __scope__) {
            scopeData = ((DefaultScope) __scope__).getData();
        }
    }

    @Producer(scope = CustomScope.class)
    public static class MetaCustomScopeEntity {
        String value = "custom scope";
        String scopeData = null;

        public MetaCustomScopeEntity(Object __scope__) {
            scopeData = ((CustomScope) __scope__).getData();
        }
    }

    public static class MetaScopeEntityHolder {
        @Inject
        MetaDefaultScopeEntity scopeEntity;
        @Inject
        MetaExtScopeEntity extScopeEntity;
        @Inject
        MetaCustomScopeEntity customScopeEntity;
    }

    @Test
    public void testScope() {
        logger.debug("testScope()");

        MetaScopeEntityHolder holder = new MetaScopeEntityHolder();

        MetaHelper.injectMeta(holder);
        assertThat(holder.scopeEntity, notNullValue());
        assertThat(holder.scopeEntity.value, is("scope"));
        assertThat(holder.scopeEntity.scopeData, is("default scope data"));
        assertThat(holder.extScopeEntity, nullValue());
        assertThat(holder.customScopeEntity, nullValue());
        holder.scopeEntity = null;

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtScope()), holder);
        assertThat(holder.scopeEntity, notNullValue());
        assertThat(holder.scopeEntity.value, is("scope"));
        assertThat(holder.scopeEntity.scopeData, is("ext scope data"));
        assertThat(holder.extScopeEntity, notNullValue());
        assertThat(holder.extScopeEntity.value, is("ext scope"));
        assertThat(holder.extScopeEntity.scopeData, is("ext scope data"));
        assertThat(holder.customScopeEntity, nullValue());
        holder.scopeEntity = null;
        holder.extScopeEntity = null;

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new CustomScope()), holder);
        assertThat(holder.customScopeEntity, notNullValue());
        assertThat(holder.customScopeEntity.value, is("custom scope"));
        assertThat(holder.customScopeEntity.scopeData, is("custom scope data"));
        assertThat(holder.scopeEntity, nullValue());
        assertThat(holder.extScopeEntity, nullValue());
    }

    public static class MetaFactoryScopeEntityHolder {
        @Inject
        DefaultScopeMetaFactory defaultScopeFactory;
        @Inject
        ExtScopeMetaFactory extScopeFactory;
        @Inject
        CustomScopeMetaFactory customScopeFactory;
        @Inject
        MixedScopeMetaFactory mixedScopeFactory;

        @Factory
        interface DefaultScopeMetaFactory {
            MetaDefaultScopeEntity get();
        }

        @Factory
        interface ExtScopeMetaFactory {
            MetaExtScopeEntity get();
        }

        @Factory
        interface CustomScopeMetaFactory {
            MetaCustomScopeEntity get();
        }

        @Factory
        interface MixedScopeMetaFactory {
            MetaDefaultScopeEntity getDefault();

            MetaExtScopeEntity getExt();

            MetaCustomScopeEntity getCustom();
        }
    }

    @Test
    public void testFactoryScope() {
        logger.debug("testFactoryScope()");

        MetaFactoryScopeEntityHolder holder = new MetaFactoryScopeEntityHolder();

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new CustomScope()), holder);
        assertThat(holder.customScopeFactory, notNullValue());
        assertThat(holder.customScopeFactory.get(), notNullValue());
        assertThat(holder.defaultScopeFactory, nullValue());
        assertThat(holder.extScopeFactory, nullValue());

        assertThat(holder.mixedScopeFactory, notNullValue());
        assertThat(holder.mixedScopeFactory.getCustom(), notNullValue());
        assertThat(holder.mixedScopeFactory.getDefault(), nullValue());
        assertThat(holder.mixedScopeFactory.getExt(), nullValue());

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtScope()), holder);
        assertThat(holder.customScopeFactory, notNullValue());
        assertThat(holder.customScopeFactory.get(), notNullValue());
        assertThat(holder.extScopeFactory, notNullValue());
        assertThat(holder.extScopeFactory.get(), notNullValue());
        assertThat(holder.defaultScopeFactory, notNullValue());
        assertThat(holder.defaultScopeFactory.get(), notNullValue());
        assertThat(holder.mixedScopeFactory, notNullValue());
        assertThat(holder.mixedScopeFactory.getDefault(), notNullValue());
        assertThat(holder.mixedScopeFactory.getExt(), notNullValue());
        assertThat(holder.mixedScopeFactory.getCustom(), nullValue());
    }

    @Producer
    public static class EntitySix {
        String value = "six";

        public String getValue() {
            return value;
        }
    }

    @Producer(ext = EntitySix.class, scope = ExtScope.class)
    public static class EntitySixExt extends EntitySix {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    @Producer(ext = EntitySixExt.class, scope = ExtExtScope.class)
    public static class EntitySixExtExt extends EntitySixExt {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    @Producer
    public static class EntitySeven {
        String value = "seven";

        public String getValue() {
            return value;
        }
    }

    @Producer(ext = EntitySeven.class, scope = ExtExtScope.class)
    public static class EntitySevenExtExt extends EntitySeven {
        public String getValue() {
            return super.getValue() + " ext ext";
        }
    }

    public static class ExtMetaHolder {
        @Inject
        public EntitySix entitySix;
        @Inject
        public EntitySeven entitySeven;
    }

    @Test
    public void testMetaExt() {
        logger.debug("testMetaExt()");

        ExtMetaHolder holder = new ExtMetaHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entitySix, notNullValue());
        assertThat(holder.entitySix.getValue(), is("six"));
        assertThat(holder.entitySeven, notNullValue());
        assertThat(holder.entitySeven.getValue(), is("seven"));
        holder.entitySix = null;
        holder.entitySeven = null;

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtScope()), holder);
        assertThat(holder.entitySix, notNullValue());
        assertThat(holder.entitySix.getValue(), is("six ext"));
        assertThat(holder.entitySeven, notNullValue());
        assertThat(holder.entitySeven.getValue(), is("seven"));
        holder.entitySix = null;
        holder.entitySeven = null;

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtExtScope()), holder);
        assertThat(holder.entitySix, notNullValue());
        assertThat(holder.entitySix.getValue(), is("six ext ext"));
        assertThat(holder.entitySeven, notNullValue());
        assertThat(holder.entitySeven.getValue(), is("seven ext ext"));
    }

    @Producer(of = String.class)
    public static class StringProvider {
        @Constructor
        static String get() {
            return "str";
        }
    }

    @Producer(of = String.class, ext = String.class, scope = ExtScope.class)
    public static class StringProviderExt {
        @Constructor
        static String get() {
            return "ext str";
        }
    }

    public static class ExternalMetaHolder {
        @Inject
        String stringEntity;
    }

    @Test
    public void testExternalMeta() {
        logger.debug("testExternalMeta()");

        ExternalMetaHolder holder = new ExternalMetaHolder();
        MetaHelper.injectMeta(holder);

        assertThat(holder.stringEntity, notNullValue());
        assertThat(holder.stringEntity, is("str"));

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtScope()), holder);

        assertThat(holder.stringEntity, notNullValue());
        assertThat(holder.stringEntity, is("ext str"));

    }

    public static class SchemeEntity {

    }

    @Producer(of = SchemeEntity.class)
    public interface MetaScheme {
        @Constructor
        SchemeEntity get();
    }

    @Producer(of = SchemeEntity.class, ext = SchemeEntity.class, scope = ExtScope.class)
    public static class SchemeEntityProvider {
        @Constructor
        public static SchemeEntity get() {
            return new SchemeEntity();
        }
    }

    @Producer(of = SchemeEntity.class, ext = SchemeEntity.class, scope = ExtExtScope.class)
    public static class ExtSchemeEntityProvider {
        @Constructor
        public static SchemeEntity get() {
            return new SchemeEntity();
        }
    }

    public static class MetaSchemeHolder {
        @Inject
        SchemeEntity entity;
    }

    @Test
    public void testMetaScheme() {
        logger.debug("testMetaScheme()");

        MetaSchemeHolder holder = new MetaSchemeHolder();
        MetaHelper.injectMeta(holder);
        Assert.assertThat(holder.entity, nullValue());

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtScope()), holder);
        Assert.assertThat(holder.entity, notNullValue());
    }

    // test ClassForNameMetasitory here. there are compatable entities right above
    @Test
    public void testClassForNameMetasitory() {
        logger.debug("testClassForNameMetasitor()");

        ClassForNameMetasitory metasitory = new ClassForNameMetasitory();

        Criteria criteria = new Criteria.Builder().masterEq(EntitySixExt.class).build();
        List<Metacode<?>> items = metasitory.search(criteria);
        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertTrue(items.get(0).getMasterClass() == EntitySixExt.class);

        criteria = new Criteria.Builder().masterEqDeep(EntitySixExtExt.class).build();
        items = metasitory.search(criteria);
        assertThat(items, notNullValue());
        assertThat(items.size(), is(3));
        assertTrue(items.get(0).getMasterClass() == EntitySixExtExt.class);
        assertTrue(items.get(1).getMasterClass() == EntitySixExt.class);
        assertTrue(items.get(2).getMasterClass() == EntitySix.class);
    }

    public static class MetaMethodEntityHolder {
        EntityOne entity;
        Lazy<EntityOne> lazy;
        Provider<EntityOne> provider;

        static Class<? extends EntityOne> clazz;

        @Inject
        EntityOne entity2;

        EntityOne factoryEntity1;
        EntityOne factoryEntity2;
        EntityOne factoryEntity3;

        EntityOne scopeEntity1;
        MetaCustomScopeEntity scopeEntity2;

        EntitySix extEntity;

        @Inject
        void inject(EntitySix entity1) {
            this.extEntity = entity1;
        }

        @Factory
        public interface MetaFactory {
            EntityOne get();
        }

        @Factory
        public interface MetaFactory2 {
            EntityOne get();
        }

        @Inject
        void inject(MetaFactory factory) {
            this.factoryEntity1 = factory.get();
        }

        @Inject
        void inject(MetaFactory factory, MetaFactory2 factory2) {
            this.factoryEntity2 = factory.get();
            this.factoryEntity3 = factory2.get();
        }

        @Inject
        void inject(EntityOne param1) {
            this.entity = param1;
        }

        @Inject
        void inject(Lazy<EntityOne> param1, Provider<EntityOne> provider) {
            this.lazy = param1;
            this.provider = provider;
        }

        @Inject
        static void inject(Class<? extends EntityOne> clazz) {
            MetaMethodEntityHolder.clazz = clazz;
        }

        @Inject
        void inject(EntityOne entity1, MetaCustomScopeEntity entity2) {
            this.scopeEntity1 = entity1;
            this.scopeEntity2 = entity2;
        }
    }

    @Test
    public void testMethodInject() {
        logger.debug("testMethodInject()");

        MetaMethodEntityHolder holder = new MetaMethodEntityHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, is("one"));

        assertThat(holder.lazy, notNullValue());
        assertThat(holder.lazy.get().value, is("one"));

        assertThat(holder.provider, notNullValue());
        assertThat(holder.provider.get().value, is("one"));

        assertTrue(holder.lazy.get() == holder.lazy.get());
        assertFalse(holder.provider.get() == holder.provider.get());

        assertFalse(holder.entity == holder.lazy.get());
        assertFalse(holder.entity == holder.provider.get());
        assertFalse(holder.lazy.get() == holder.provider.get());

        assertThat(holder.entity2, notNullValue());
        assertThat(holder.entity2.value, is("one"));

        assertThat(holder.extEntity, notNullValue());
        assertThat(holder.extEntity.getValue(), is("six"));

        assertThat(holder.factoryEntity1, notNullValue());
        assertThat(holder.factoryEntity1.value, is("one"));
        assertThat(holder.factoryEntity2, notNullValue());
        assertThat(holder.factoryEntity2.value, is("one"));
        assertThat(holder.factoryEntity3, notNullValue());
        assertThat(holder.factoryEntity3.value, is("one"));

        assertThat(holder.scopeEntity1, notNullValue());
        assertThat(holder.scopeEntity1.value, is("one"));
        assertThat(holder.scopeEntity2, nullValue());

        MetaHelper.injectMeta(MetaHelper.getMetaScope(new CustomScope()), holder);
        assertThat(holder.scopeEntity1, nullValue());
        assertThat(holder.scopeEntity2, notNullValue());

        holder.extEntity = null;
        MetaHelper.injectMeta(MetaHelper.getMetaScope(new ExtScope()), holder);
        assertThat(holder.extEntity, notNullValue());
        assertThat(holder.extEntity.getValue(), is("six ext"));

        MetaHelper.injectStaticMeta(MetaMethodEntityHolder.clazz);
        assertThat(MetaMethodEntityHolder.clazz, notNullValue());
        assertEquals(MetaMethodEntityHolder.clazz, EntityOne.class);
    }
}
