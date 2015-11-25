package com.github.brooth.metacode.samples.validate;

import com.github.brooth.metacode.samples.MetaHelper;
import com.github.brooth.metacode.validate.*;

import java.util.Set;

/**
 *
 */
public class ValidatorSample {

    @MetacodeValidator(
            emitExpression = "%m.age > 18",
            emitError = "too young"
    )
    public interface AgeValidator extends IValidator {
    }

    @MetacodeValidator(
            emitExpression = "%m.age - 18 >= %m.experience",
            emitError = "too high experience"
    )
    public interface ExperienceValidator extends IValidator {
    }

    public static class HireAction {
        @Validate(NotBlank.class)
        public String name;
        @Validate(NotEmpty.class)
        public String[] degrees;
        @Validate(AgeValidator.class)
        public int age;
        @Validate(ExperienceValidator.class)
        public int experience;

        public HireAction(String name, int age, int experience, String... degrees) {
            this.name = name;
            this.degrees = degrees;
            this.age = age;
            this.experience = experience;
        }

        public void execute() {
            MetaHelper.validate(this);
            //...
        }

        public Set<String> isAppropriate() {
            return MetaHelper.validateSafe(this);
        }
    }

    public static void main(String[] args) {
        new HireAction("John Smith", 45, 15, "Master Chef").execute();

        Set<String> incompatibilities = new HireAction(null, 17, 15).isAppropriate();
        for (String incompatibility : incompatibilities) {
            System.err.println(incompatibility);
        }
    }
}
