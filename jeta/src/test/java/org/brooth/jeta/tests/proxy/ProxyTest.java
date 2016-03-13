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

package org.brooth.jeta.tests.proxy;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.TestMetaHelper;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.proxy.AbstractProxy;
import org.brooth.jeta.proxy.Proxy;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ProxyTest extends BaseTest {

    @Log
    Logger logger;

    public interface IFoo {
        String value1();

        int value2();

        Class<?> value3();
    }

    public static class Foo implements IFoo {
        public String value1() {
            return "foo";
        }

        public int value2() {
            return 12;
        }

        public Class<?> value3() {
            return Foo.class;
        }
    }

    public static abstract class Value1Foo implements IFoo, AbstractProxy<IFoo> {
        public String value1() {
            return real().value1() + " proxy";
        }
    }

    public static abstract class Value2Foo implements IFoo, AbstractProxy<IFoo> {
        public int value2() {
            return real().value2() + 7;
        }
    }

    public static abstract class Value3Foo implements IFoo, AbstractProxy<IFoo> {
        public Class<?> value3() {
            return Value3Foo.class;
        }
    }

    public static abstract class AllValueFoo implements IFoo, AbstractProxy<IFoo> {
        public String value1() {
            return "all";
        }

        public int value2() {
            return 100;
        }

        public Class<?> value3() {
            return Boolean.class;
        }
    }

    @Proxy(ProxyTest.Value1Foo.class)
    IFoo foo1;
    @Proxy(ProxyTest.Value2Foo.class)
    IFoo foo2;
    @Proxy(ProxyTest.Value3Foo.class)
    IFoo foo3;
    @Proxy(ProxyTest.AllValueFoo.class)
    IFoo foo4;

    @Test
    public void testProxy() {
        logger.debug("testProxy()");

        assertThat(foo1, nullValue());
        assertThat(foo2, nullValue());
        assertThat(foo3, nullValue());

        IFoo foo = new Foo();

        foo1 = foo;
        assertThat(foo1.value1(), is("foo"));
        assertThat(foo1.value2(), is(12));
        assertEquals(foo1.value3(), Foo.class);

        TestMetaHelper.createProxy(this, foo1);
        assertThat(foo1.value1(), is("foo proxy"));
        assertThat(foo1.value2(), is(12));
        assertEquals(foo1.value3(), Foo.class);

        foo2 = foo;
        assertThat(foo2.value1(), is("foo"));
        assertThat(foo2.value2(), is(12));
        assertEquals(foo2.value3(), Foo.class);

        TestMetaHelper.createProxy(this, foo2);
        assertThat(foo2.value1(), is("foo"));
        assertThat(foo2.value2(), is(19));
        assertEquals(foo2.value3(), Foo.class);

        foo3 = foo;
        assertThat(foo3.value1(), is("foo"));
        assertThat(foo3.value2(), is(12));
        assertEquals(foo3.value3(), Foo.class);

        TestMetaHelper.createProxy(this, foo3);
        assertThat(foo3.value1(), is("foo"));
        assertThat(foo3.value2(), is(12));
        assertEquals(foo3.value3(), Value3Foo.class);

        foo4 = foo;
        TestMetaHelper.createProxy(this, foo4);
        assertThat(foo4.value1(), is("all"));
        assertThat(foo4.value2(), is(100));
        assertEquals(foo4.value3(), Boolean.class);
    }
}
