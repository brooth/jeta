/*
 * Copyright 2015 Oleg Khalidov
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

package org.javameta.tests.validate;

import org.javameta.BaseTest;
import org.javameta.Logger;
import org.javameta.TestMetaHelper;
import org.javameta.log.Log;
import org.javameta.validate.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaValidatorsTest extends BaseTest {

    @Log
    Logger logger;

    public static class SimpleValidatorHolder {
        @MetaValidator(emitExpression = "$f > 0 && $f < 5", emitError = "$n must be in [1..5]")
        public interface RangeValidator extends Validator {
        }

        @Validate(RangeValidator.class)
        public int value = 0;
    }

    @Test
    public void testSimpleValidator() {
        logger.debug("testSimpleValidator()");

        SimpleValidatorHolder holder = new SimpleValidatorHolder();
        ValidationController controller = TestMetaHelper.validationController(holder);
        String error = checkThrows(controller);
        logger.debug("error: '%s'", error);
        assertThat(error, is("value must be in [1..5]"));

        holder.value = 3;
        controller.validate();
        assertThat(controller.validateSafe(), empty());

        holder.value = 6;
        logger.debug("error: '%s'", error);
        assertThat(error, is("value must be in [1..5]"));
    }

    public static class ComplexValidatorHolder {
        @MetaValidator(emitExpression = "$f >= 0 && $f < 150", emitError = "incorrect $n ${$f}")
        public interface AgeValidator extends Validator {
        }

        @MetaValidator(
                emitExpression = "$f >= 0 && $f < $m.age-18",
                emitError = "incorrect $n ${$f}, ${\"must\".toUpperCase()} be less than ${$m.age-18}"
        )
        public interface ExperienceValidator extends Validator {
        }

        @Validate(AgeValidator.class)
        public int age = 30;

        @Validate(ExperienceValidator.class)
        public int experience = 10;
    }

    @Test
    public void testComplexValidator() {
        logger.debug("testComplexValidator()");

        ComplexValidatorHolder holder = new ComplexValidatorHolder();
        ValidationController controller = TestMetaHelper.validationController(holder);
        controller.validate();

        holder.age = 38;
        holder.experience = 30;
        String error = checkThrows(controller);
        logger.debug("error: '%s'", error);
        assertThat(error, is("incorrect experience 30, MUST be less than 20"));
    }

    private String checkThrows(ValidationController controller) {
        try {
            controller.validate();
            assertTrue(false);
        } catch (ValidationException e) {
            assertTrue(true);
            return e.getMessage();
        }
        return null;
    }
}
