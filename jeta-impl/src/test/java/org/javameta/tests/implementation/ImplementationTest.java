package org.javameta.tests.implementation;

import org.javameta.BaseTest;
import org.javameta.Logger;
import org.javameta.TestMetaHelper;
import org.javameta.log.Log;
import org.javameta.util.ImplementationController;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ImplementationTest extends BaseTest {

    @Log
    Logger logger;

    @Test
    public void testFooImplementer() {
        logger.debug("testFooImplementer()");

        ImplementationController<AbstractFoo> controller = TestMetaHelper.implementationController(AbstractFoo.class);
        assertThat(controller, notNullValue());
        assertThat(controller.hasImplementation(), is(true));

        Collection<AbstractFoo> all = controller.getImplementations();
        assertThat(all, hasSize(1));
        // same state
        assertThat(controller.getImplementations(), hasSize(1));
        assertThat(controller.hasImplementation(), is(true));

        AbstractFoo impl = controller.getImplementation();
        assertThat(impl, notNullValue());
        assertThat(impl, instanceOf(FooImplementer.class));

        logger.debug("value is :" + impl.value());
        assertThat(impl.value(), is(new FooImplementer().value()));

        // each invoke - new instance
        assertThat(impl, not(is(controller.getImplementation())));
        // but the type still the same
        assertThat(impl, instanceOf(controller.getImplementation().getClass()));
    }

    @Test
    public void testNoImplementation() {
        logger.debug("testNoImplementation()");

        ImplementationController<AbstractNone> controller = TestMetaHelper.implementationController(AbstractNone.class);
        assertThat(controller, notNullValue());
        assertThat(controller.hasImplementation(), is(false));
        assertThat(controller.getImplementation(), is(nullValue()));
        assertThat(controller.getImplementations(), hasSize(0));
    }

    @Test
    public void testPriority() {
        logger.debug("testPriority()");

        ImplementationController<AbstractPriority> controller = TestMetaHelper.implementationController(AbstractPriority.class);
        assertThat(controller, notNullValue());
        assertThat(controller.hasImplementation(), is(true));

        assertThat(controller.getImplementation(), instanceOf(PriorityHighImplementer.class));

        Collection<AbstractPriority> all = controller.getImplementations();
        assertThat(all, hasSize(3));

        Iterator<AbstractPriority> iter = all.iterator();
        assertThat(iter.next(), instanceOf(PriorityHighImplementer.class));
        assertThat(iter.next(), instanceOf(PriorityMediumImplementer.class));
        assertThat(iter.next(), instanceOf(PriorityLowImplementer.class));
    }
}
