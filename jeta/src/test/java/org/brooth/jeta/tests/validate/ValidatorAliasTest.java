package org.brooth.jeta.tests.validate;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.MetaHelper;
import org.brooth.jeta.log.Log;
import org.brooth.jeta.validate.ValidationController;
import org.brooth.jeta.validate.alias.NotBlank;
import org.brooth.jeta.validate.alias.NotEmpty;
import org.brooth.jeta.validate.alias.NotNull;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ValidatorAliasTest extends BaseTest {

    @Log
    Logger logger;

    public static class BaseAliasesHolder {
        @NotNull
        Object notNullObject;
        @NotBlank
        String notBlankString;
        @NotEmpty
        String notEmptyString;
    }

    @Test
    public void testBaseValidatorAliases() {
        logger.debug("testBaseValidatorAliases()");

        BaseAliasesHolder holder = new BaseAliasesHolder();
        ValidationController controller = MetaHelper.validationController(holder);
        List<String> errors = controller.validateSafe();
        assertThat(errors, allOf(is(notNullValue()), hasSize(3)));

        holder.notNullObject = new Object();
        holder.notBlankString = ".";
        holder.notEmptyString = " ";
        errors = controller.validateSafe();
        assertThat(errors, hasSize(0));
    }

    public static class CustomAliasHolder {
        @Nonnull
        Object value;
    }

    @Test
    public void testCustomValidatorAlias() {
        logger.debug("testCustomValidatorAlias()");

        CustomAliasHolder holder = new CustomAliasHolder();
        Set<Class<? extends Annotation>> validators = new HashSet<>();
        validators.add(Nonnull.class);
        ValidationController controller = MetaHelper.validationController(holder, validators);
        List<String> errors = controller.validateSafe();
        assertThat(errors, hasSize(1));

        holder.value = new Object();
        errors = controller.validateSafe();
        assertThat(errors, hasSize(0));
    }
}
