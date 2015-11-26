package org.javameta.samples.validate;

import org.javameta.samples.MetaHelper;
import org.javameta.validate.*;

import java.util.List;

/**
 *
 */
public class ValidatorSample {

    @MetaValidator(
            emitExpression = "%m.age > 18",
            emitError = "Too young" // %{I18n.get(\"TOO_YOUNG\")}
    )
    public interface AgeValidator extends Validator {
    }

    @MetaValidator(
            emitExpression = "%m.age - 18 >= %m.experience",
            emitError = "%m.experience years of experience is too high for the age of %m.age" // %m.getAge(); 
    )
    public interface ExperienceValidator extends Validator {
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
            System.out.println(name + ", see you monday!");
        }

        public List<String> isAppropriate() {
            return MetaHelper.validateSafe(this);
        }
    }

    public static void main(String[] args) {
        new HireAction("John Smith", 45, 15, "Master Chef").execute();

        List<String> incompatibilities = new HireAction(null, 17, 15).isAppropriate();
        if(incompatibilities.size() > 0) {
            System.out.println("Oops, ");
            for (String incompatibility : incompatibilities) {
                System.out.println(incompatibility);
            }
        }
    }
}
