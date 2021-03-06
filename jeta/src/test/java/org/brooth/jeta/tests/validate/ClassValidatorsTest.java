/*
 * Copyright 2016 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta.tests.validate;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.MetaHelper;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.validate.*;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ClassValidatorsTest extends BaseTest {

    @Log
    Logger logger;

    public static class NotNullObjectHolder {
        @Validate(NotNull.class)
        Object notNullObject;
    }

    @Test
    public void testNotNullValidator() {
        logger.debug("testNotNullValidator()");

        NotNullObjectHolder holder = new NotNullObjectHolder();
        ValidationController controller = MetaHelper.validationController(holder);
        logger.debug("error: '%s'", checkThrows(controller));

        List<String> errors = controller.validateSafe();
        assertThat(errors, allOf(is(notNullValue()), hasSize(1)));

        holder.notNullObject = new Object();
        controller.validate();
        assertThat(controller.validateSafe(), is(empty()));
    }

    public static class NotBlankStringHolder {
        @Validate(NotBlank.class)
        String notBlankString;
    }

    @Test
    public void testNotBlankValidator() {
        logger.debug("testNotBlankValidator()");

        NotBlankStringHolder holder = new NotBlankStringHolder();
        ValidationController controller = MetaHelper.validationController(holder);
        logger.debug("error: '%s'", checkThrows(controller));

        List<String> errors = controller.validateSafe();
        assertThat(errors, allOf(is(notNullValue()), hasSize(1)));

        holder.notBlankString = " \t";
        logger.debug("error: '%s'", checkThrows(controller));

        errors = controller.validateSafe();
        assertThat(errors, allOf(is(notNullValue()), hasSize(1)));

        holder.notBlankString = ".";
        controller.validate();
        assertThat(controller.validateSafe(), is(empty()));
    }

    public static class NotEmptyHolder {
        @Validate(NotEmpty.class)
        String notEmptyString;
        @Validate(NotEmpty.class)
        Object[] notEmptyArray;
        @Validate(NotEmpty.class)
        Collection<Object> notEmptyCollection;
        @Validate(NotEmpty.class)
        Map<Object, ?> notEmptyMap;
    }

    @Test
    public void testNotEmptyValidator() {
        logger.debug("testNotEmptyValidator()");

        NotEmptyHolder holder = new NotEmptyHolder();
        ValidationController controller = MetaHelper.validationController(holder);
        logger.debug("error: '%s'", checkThrows(controller));

        List<String> errors = controller.validateSafe();
        assertThat(errors, allOf(is(notNullValue()), hasSize(4)));

        holder.notEmptyString = "";
        assertThat(controller.validateSafe(), hasSize(4));

        holder.notEmptyString = "\t";
        assertThat(controller.validateSafe(), hasSize(3));

        holder.notEmptyArray = new Object[0];
        assertThat(controller.validateSafe(), hasSize(3));

        holder.notEmptyArray = new Object[1];
        assertThat(controller.validateSafe(), hasSize(2));

        holder.notEmptyCollection = Collections.emptyList();
        assertThat(controller.validateSafe(), hasSize(2));

        holder.notEmptyCollection = Collections.singleton(new Object());
        assertThat(controller.validateSafe(), hasSize(1));

        holder.notEmptyMap = Collections.emptyMap();
        assertThat(controller.validateSafe(), hasSize(1));

        holder.notEmptyMap = Collections.singletonMap(new Object(), null);
        assertThat(controller.validateSafe(), empty());
        controller.validate();

        holder.notEmptyString = null;
        logger.debug("error: '%s'", checkThrows(controller));
        assertThat(controller.validateSafe(), hasSize(1));
    }

    public static class MultiValidatorHolder {
        @Validate({NotNull.class, NotEmpty.class, NotBlank.class})
        String value;
    }

    @Test
    public void testMultipleValidators() {
        logger.debug("testMultipleValidators()");

        MultiValidatorHolder holder = new MultiValidatorHolder();
        ValidationController controller = MetaHelper.validationController(holder);
        logger.debug(String.format("error: '%s'", checkThrows(controller)));
        assertThat(controller.validateSafe(), hasSize(3));

        holder.value = "";
        logger.debug(String.format("error: '%s'", checkThrows(controller)));
        assertThat(controller.validateSafe(), hasSize(2));

        holder.value = "\t";
        logger.debug(String.format("error: '%s'", checkThrows(controller)));
        assertThat(controller.validateSafe(), hasSize(1));

        holder.value = ".";
        assertThat(controller.validateSafe(), empty());
        controller.validate();
    }

    private String checkThrows(ValidationController controller) {
        try {
            controller.validate();
            assertTrue(false);
        } catch (ValidationException e) {
            return e.getMessage();
        }
        return null;
    }
}
