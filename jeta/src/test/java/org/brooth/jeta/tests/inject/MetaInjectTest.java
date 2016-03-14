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
import org.brooth.jeta.inject.Meta;
import org.brooth.jeta.inject.MetaEntity;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.metasitory.ClassForNameMetasitory;
import org.brooth.jeta.metasitory.Criteria;
import org.junit.Test;

import javax.inject.Inject;
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
        @Meta
        MetaEntityOne entity;
        @Meta
        Class<? extends MetaEntityOne> clazz;
        @Meta
        Lazy<MetaEntityOne> lazy;
        @Meta
        Provider<MetaEntityOne> provider;
    }

    @Test
    public void testSimpleInject() {
        logger.debug("testSimpleInject()");

        MetaEntityHolder holder = new MetaEntityHolder();
        TestMetaHelper.injectMeta(holder);
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

    public static class MetaAliasEntityHolder {
        @Inject
        MetaEntityOne entity;
        @Inject
        Class<? extends MetaEntityOne> clazz;
        @Inject
        Lazy<MetaEntityOne> lazy;
        @Inject
        Provider<MetaEntityOne> provider;
        @Inject
        javax.inject.Provider<MetaEntityOne> javaxProvider;
    }

    @Test
    public void testAliasInject() {
        logger.debug("testAliasInject()");

        MetaAliasEntityHolder holder = new MetaAliasEntityHolder();
        TestMetaHelper.injectMeta(holder);
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
        assertThat(holder.javaxProvider.get().value, is("one"));
    }

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
        @Meta
        MetaEntityTwo entityTwo;
        @Meta
        MetaEntityThree entityThree;
        @Meta
        MetaEntityFour entityFour;
    }

    @Test
    public void testMetaProvider() {
        logger.debug("testMetaProvider()");

        MetaProviderHolder holder = new MetaProviderHolder();
        TestMetaHelper.injectMeta(holder);
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
        @Meta
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
        TestMetaHelper.injectMeta(holder);
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
        @Meta
        MetaDefaultScopeEntity scopeEntity;
        @Meta
        MetaExtScopeEntity extScopeEntity;
        @Meta
        MetaCustomScopeEntity customScopeEntity;
    }

    @Test
    public void testScope() {
        logger.debug("testScope()");

        MetaScopeEntityHolder holder = new MetaScopeEntityHolder();

        TestMetaHelper.injectMeta(holder);
        assertThat(holder.scopeEntity, notNullValue());
        assertThat(holder.scopeEntity.value, is("scope"));
        assertThat(holder.scopeEntity.scopeData, is("default scope data"));
        assertThat(holder.extScopeEntity, nullValue());
        assertThat(holder.customScopeEntity, nullValue());
        holder.scopeEntity = null;

        TestMetaHelper.injectMeta(TestMetaHelper.getMetaScope(new ExtScope()), holder);
        assertThat(holder.scopeEntity, notNullValue());
        assertThat(holder.scopeEntity.value, is("scope"));
        assertThat(holder.scopeEntity.scopeData, is("ext scope data"));
        assertThat(holder.extScopeEntity, notNullValue());
        assertThat(holder.extScopeEntity.value, is("ext scope"));
        assertThat(holder.extScopeEntity.scopeData, is("ext scope data"));
        assertThat(holder.customScopeEntity, nullValue());
        holder.scopeEntity = null;
        holder.extScopeEntity = null;

        TestMetaHelper.injectMeta(TestMetaHelper.getMetaScope(new CustomScope()), holder);
        assertThat(holder.customScopeEntity, notNullValue());
        assertThat(holder.customScopeEntity.value, is("custom scope"));
        assertThat(holder.customScopeEntity.scopeData, is("custom scope data"));
        assertThat(holder.scopeEntity, nullValue());
        assertThat(holder.extScopeEntity, nullValue());
    }

    public static class MetaFactoryScopeEntityHolder {
        @Meta
        DefaultScopeMetaFactory defaultScopeFactory;
        @Meta
        ExtScopeMetaFactory extScopeFactory;
        @Meta
        CustomScopeMetaFactory customScopeFactory;

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
    }

    @Test
    public void testFactoryScope() {
        logger.debug("testFactoryScope()");

        MetaFactoryScopeEntityHolder holder = new MetaFactoryScopeEntityHolder();

        TestMetaHelper.injectMeta(TestMetaHelper.getMetaScope(new CustomScope()), holder);
        assertThat(holder.customScopeFactory, notNullValue());
        assertThat(holder.customScopeFactory.get(), notNullValue());
        assertThat(holder.defaultScopeFactory, nullValue());
        assertThat(holder.extScopeFactory, nullValue());

        TestMetaHelper.injectMeta(TestMetaHelper.getMetaScope(new ExtScope()), holder);
        assertThat(holder.customScopeFactory, notNullValue());
        assertThat(holder.customScopeFactory.get(), notNullValue());
        assertThat(holder.extScopeFactory, notNullValue());
        assertThat(holder.extScopeFactory.get(), notNullValue());
        assertThat(holder.defaultScopeFactory, notNullValue());
        assertThat(holder.defaultScopeFactory.get(), notNullValue());
    }

    @MetaEntity
    public static class MetaEntitySix {
        String value = "six";

        public String getValue() {
            return value;
        }
    }

    @MetaEntity(ext = MetaEntitySix.class, extScope = DefaultScope.class)
    public static class WeakMetaEntitySixExt extends MetaEntitySix {
        public String getValue() {
            throw new IllegalStateException();
        }
    }

    @MetaEntity(ext = MetaEntitySix.class, extScope = DefaultScope.class)
    public static class MetaEntitySixExt extends MetaEntitySix {
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

    @MetaEntity(ext = MetaEntitySeven.class, extScope = DefaultScope.class)
    public static class MetaEntitySevenExt extends MetaEntitySeven {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    @MetaEntity(ext = MetaEntitySevenExt.class, extScope = DefaultScope.class)
    public static class MetaEntitySevenExtExt extends MetaEntitySevenExt {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    public static class ExtMetaHolder {
        @Meta
        public MetaEntitySix entity;
        @Meta
        public MetaEntitySeven entitySeven;
    }

    @Test
    public void testMetaExt() {
        logger.debug("testMetaExt()");

        //todo:
//        ExtMetaHolder holder = new ExtMetaHolder();
//        TestMetaHelper.injectMeta(holder);
//
//        assertThat(holder.entity, notNullValue());
//        assertThat(holder.entity.getValue(), is("six ext"));
//
//        assertThat(holder.entitySeven, notNullValue());
//        assertThat(holder.entitySeven.getValue(), is("seven ext ext"));
    }

    @MetaEntity(of = String.class)
    public static class StringProvider {
        @Constructor
        static String get() {
            return "str";
        }
    }

    public static class ExternalMetaHolder {
        @Meta
        String stringEntity;
    }

    @Test
    public void testExternalMeta() {
        logger.debug("testExternalMeta()");

        ExternalMetaHolder holder = new ExternalMetaHolder();
        TestMetaHelper.injectMeta(holder);

        assertThat(holder.stringEntity, notNullValue());
        assertThat(holder.stringEntity, is("str"));
    }

    // test ClassForNameMetasitory here. there are compatable entities right above
    @Test
    public void testClassForNameMetasitory() {
        logger.debug("testClassForNameMetasitor()");

        ClassForNameMetasitory metasitory = new ClassForNameMetasitory();

        Criteria criteria = new Criteria.Builder().masterEq(MetaEntitySevenExt.class).build();
        List<IMetacode<?>> items = metasitory.search(criteria);
        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertTrue(items.get(0).getMasterClass() == MetaEntitySevenExt.class);

        criteria = new Criteria.Builder().masterEqDeep(MetaEntitySevenExtExt.class).build();
        items = metasitory.search(criteria);
        assertThat(items, notNullValue());
        assertThat(items.size(), is(3));
        assertTrue(items.get(0).getMasterClass() == MetaEntitySevenExtExt.class);
        assertTrue(items.get(1).getMasterClass() == MetaEntitySevenExt.class);
        assertTrue(items.get(2).getMasterClass() == MetaEntitySeven.class);
    }
}
