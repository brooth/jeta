package org.brooth.jeta.tests.inject;

import org.brooth.jeta.*;
import org.brooth.jeta.inject.Meta;
import org.brooth.jeta.inject.MetaEntity;
import org.brooth.jeta.log.Log;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * @author khalidov
 * @version $Id$
 */
public class MetaInjectTest {

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
    }

    @MetaEntity(scope = CustomScope.class)
    public static class MetaScopeEntity {
        String value = "scope";
        String scopeData = null;

        public MetaScopeEntity(CustomScope scope) {
            scopeData = scope.data;
        }

        public MetaScopeEntity(ExtScope scope) {
            assertTrue(false);
        }

        public MetaScopeEntity(String value, CustomScope scope, boolean fake) {
            this.value = value;
            this.scopeData = scope.data;
        }
    }

    @Test
    public void testScope() {
        logger.debug("testScope()");
    }

    @MetaEntity(scope = DefaultScope.class)
    public static class MetaDefaultScopeEntity {
        String value = "default";
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
    }

    @MetaEntity(of = String.class)
    public static class StringProvider {
        @Constructor
        static String get() {
            return "boo";
        }
    }

    @MetaEntity(of = String.class, scope = ExtScope.class)
    public static class ExtStringProvider {
        @Constructor
        static String get() {
            return "foo";
        }
    }

    /*
     * meta entity extending
     */
    @MetaEntity
    public static class MetaEntitySix {
        String value = "six";

        public String getValue() {
            return value;
        }
    }

    @MetaEntity(ext = MetaEntitySix.class, priority = Integer.MIN_VALUE)
    public static class WeakMetaEntitySixExt extends MetaEntitySix {
        public String getValue() {
            throw new IllegalStateException();
        }
    }

    @MetaEntity(ext = MetaEntitySix.class, priority = Integer.MAX_VALUE)
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

    @MetaEntity(ext = MetaEntitySeven.class)
    public static class MetaEntitySevenExt extends MetaEntitySeven {
        public String getValue() {
            return super.getValue() + " ext";
        }
    }

    @MetaEntity(ext = MetaEntitySevenExt.class)
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
    }
}
