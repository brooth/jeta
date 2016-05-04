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
import org.brooth.jeta.inject.MetaEntity;
import org.brooth.jeta.inject.MetaScope;
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

    @MetaEntity
    public static class MetaEntityOne {
        String value = "one";
    }

    public static class MetaEntityHolder {
        @Inject
        MetaEntityOne entity;
        @Inject
        Class<? extends MetaEntityOne> clazz;
        @Inject
        Lazy<MetaEntityOne> lazy;
        @Inject
        Provider<MetaEntityOne> provider;
    }

    @Test
    public void testSimpleInject() {
        logger.debug("testSimpleInject()");

        MetaEntityHolder holder = new MetaEntityHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, is("one"));

        assertThat(holder.clazz, notNullValue());
        assertEquals(holder.clazz, MetaEntityOne.class);

        assertThat(holder.lazy, notNullValue());
        assertThat(holder.lazy.get().value, is("one"));

        assertThat(holder.provider, notNullValue());
        assertThat(holder.provider.get().value, is("one"));

        assertTrue(holder.lazy.get() == holder.lazy.get());
        assertFalse(holder.provider.get() == holder.provider.get());

        assertFalse(holder.entity == holder.lazy.get());
        assertFalse(holder.entity == holder.provider.get());
        assertFalse(holder.lazy.get() == holder.provider.get());
    }

    public static class StaticMetaEntityHolder {
        @Inject
        static MetaEntityOne entity;
        @javax.inject.Inject
        static MetaEntityOne aliasEntity;
        @Inject
        static Class<? extends MetaEntityOne> clazz;
        @Inject
        static Lazy<MetaEntityOne> lazy;
        @Inject
        static Provider<MetaEntityOne> provider;
    }

    @Test
    public void testStaticInject() {
        logger.debug("testStaticInject()");

        MetaHelper.injectStaticMeta(StaticMetaEntityHolder.class);
        assertThat(StaticMetaEntityHolder.entity, notNullValue());
        assertThat(StaticMetaEntityHolder.entity.value, is("one"));

        assertThat(StaticMetaEntityHolder.aliasEntity, notNullValue());
        assertThat(StaticMetaEntityHolder.aliasEntity.value, is("one"));

        assertThat(StaticMetaEntityHolder.clazz, notNullValue());
        assertEquals(StaticMetaEntityHolder.clazz, MetaEntityOne.class);

        assertThat(StaticMetaEntityHolder.lazy, notNullValue());
        assertThat(StaticMetaEntityHolder.lazy.get().value, is("one"));

        assertThat(StaticMetaEntityHolder.provider, notNullValue());
        assertThat(StaticMetaEntityHolder.provider.get().value, is("one"));

        assertTrue(StaticMetaEntityHolder.lazy.get() == StaticMetaEntityHolder.lazy.get());
        assertFalse(StaticMetaEntityHolder.provider.get() == StaticMetaEntityHolder.provider.get());

        assertFalse(StaticMetaEntityHolder.entity == StaticMetaEntityHolder.lazy.get());
        assertFalse(StaticMetaEntityHolder.entity == StaticMetaEntityHolder.provider.get());
        assertFalse(StaticMetaEntityHolder.lazy.get() == StaticMetaEntityHolder.provider.get());
    }

    @MetaEntity(singleton = true)
    public static class SingletonEntity {
        String value = "default";

        public SingletonEntity() {
        }

        public SingletonEntity(String value) {
            this.value = value;
        }
    }

    @MetaEntity(of = MetaInjectTest.SingletonEntity.class, singleton = true, scope = CustomScope.class)
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
        MetaEntityOne entity;
        @javax.inject.Inject
        Class<? extends MetaEntityOne> clazz;
        @javax.inject.Inject
        Lazy<MetaEntityOne> lazy;
        @javax.inject.Inject
        Provider<MetaEntityOne> provider;
        @javax.inject.Inject
        javax.inject.Provider<MetaEntityOne> javaxProvider;
    }

    @Test
    public void testAliasInject() {
        logger.debug("testAliasInject()");

        MetaAliasEntityHolder holder = new MetaAliasEntityHolder();
        MetaHelper.injectMeta(holder);
        assertThat(holder.entity, notNullValue());
        assertThat(holder.entity.value, is("one"));

        assertThat(holder.clazz, notNullValue());
        assertEquals(holder.clazz, MetaEntityOne.class);

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
        assertThat(holder.javaxProvider.get().value, is("one"));    }

    public static class MetaEntityTwo {
        String value;
    }

    @MetaEntity(of = MetaEntityTwo.class)
    public static class MetaEntityTwoProvider {
        @Constructor
        public MetaEntityTwo get() {
            MetaEntityTwo e = new MetaEntityTwo();
            e.value = "two";
            return e;
        }
    }

    public static class MetaEntityThree {
        String value;
    }

    @MetaEntity(of = MetaEntityThree.class)
    public static class MetaEntityThreeProvider {
        @Constructor
        public static MetaEntityThree get() {
            MetaEntityThree e = new MetaEntityThree();
            e.value = "three";
            return e;
        }
    }

    public static class MetaEntityFour {
        String value;
    }

    @MetaEntity(of = MetaEntityFour.class, staticConstructor = "getInstance")
    public static class MetaEntityFourProvider {

        public static MetaEntityFourProvider getInstance() {
            return new MetaEntityFourProvider();
        }

        @Constructor
        public MetaEntityFour get() {
            MetaEntityFour e = new MetaEntityFour();
            e.value = "four";
            return e;
        }
    }

    public static class MetaProviderHolder {
        @Inject
        MetaEntityTwo entityTwo;
        @Inject
        MetaEntityThree entityThree;
        @Inject
        MetaEntityFour entityFour;
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

    @MetaEntity
    public static class MetaEntityFive {
        String value;

        public MetaEntityFive(String value) {
            this.value = value;
        }
    }

    public static class MetaFactoryHolder {
        @Inject
        MetaFactory factory;

        @Factory
        public interface MetaFactory {
            MetaEntityFive get(String value);

            Class<? extends MetaEntityFive> getClazz();

            Lazy<MetaEntityFive> getLazy(String value);

            Provider<MetaEntityFive> getProvider(String value);
        }
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
        assertEquals(holder.factory.getClazz(), MetaEntityFive.class);

        assertThat(holder.factory.getLazy(null), notNullValue());
        assertThat(holder.factory.getLazy("lazyFive").get().value, is("lazyFive"));

        assertThat(holder.factory.getProvider(null), notNullValue());
        assertThat(holder.factory.getProvider("fiveProvider").get().value, is("fiveProvider"));
    }

    @MetaEntity
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

    @MetaEntity(scope = ExtScope.class)
    public static class MetaExtScopeEntity {
        String value = "ext scope";
        String scopeData = null;

        public MetaExtScopeEntity(Object __scope__) {
            scopeData = ((DefaultScope) __scope__).getData();
        }
    }

    @MetaEntity(scope = CustomScope.class)
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
        assertThat(holder.mixedScopeFactory, nullValue());

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
    }

    @MetaEntity
    public static class MetaEntitySix {
        String value = "six";

        public String getValue() {
            return value;
        }
    }

    @MetaEntity(ext = MetaEntitySix.class, scope = ExtScope.class)
    public static class MetaEntitySixExt extends MetaEntitySix {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    @MetaEntity(ext = MetaEntitySixExt.class, scope = ExtExtScope.class)
    public static class MetaEntitySixExtExt extends MetaEntitySixExt {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    @MetaEntity
    public static class MetaEntitySeven {
        String value = "seven";

        public String getValue() {
            return value;
        }
    }

    @MetaEntity(ext = MetaEntitySeven.class, scope = ExtExtScope.class)
    public static class MetaEntitySevenExtExt extends MetaEntitySeven {
        public String getValue() {
            return super.getValue() + " ext ext";
        }
    }

    public static class ExtMetaHolder {
        @Inject
        public MetaEntitySix entitySix;
        @Inject
        public MetaEntitySeven entitySeven;
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

    @MetaEntity(of = String.class)
    public static class StringProvider {
        @Constructor
        static String get() {
            return "str";
        }
    }

    @MetaEntity(of = String.class, ext = String.class, scope = ExtScope.class)
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

    @MetaEntity(of = SchemeEntity.class)
    public interface MetaScheme {
        @Constructor
        SchemeEntity get();
    }

    @MetaEntity(of = SchemeEntity.class, ext = SchemeEntity.class, scope = ExtScope.class)
    public static class SchemeEntityProvider {
        @Constructor
        public static SchemeEntity get() {
            return new SchemeEntity();
        }
    }

    @MetaEntity(of = SchemeEntity.class, ext = SchemeEntity.class, scope = ExtExtScope.class)
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

        Criteria criteria = new Criteria.Builder().masterEq(MetaEntitySixExt.class).build();
        List<IMetacode<?>> items = metasitory.search(criteria);
        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertTrue(items.get(0).getMasterClass() == MetaEntitySixExt.class);

        criteria = new Criteria.Builder().masterEqDeep(MetaEntitySixExtExt.class).build();
        items = metasitory.search(criteria);
        assertThat(items, notNullValue());
        assertThat(items.size(), is(3));
        assertTrue(items.get(0).getMasterClass() == MetaEntitySixExtExt.class);
        assertTrue(items.get(1).getMasterClass() == MetaEntitySixExt.class);
        assertTrue(items.get(2).getMasterClass() == MetaEntitySix.class);
    }
}
