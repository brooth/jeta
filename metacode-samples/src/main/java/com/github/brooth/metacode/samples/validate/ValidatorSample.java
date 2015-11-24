package com.github.brooth.metacode.samples.validate;

import com.github.brooth.metacode.samples.MetaHelper;
import com.github.brooth.metacode.validate.IsNotEmpty;
import com.github.brooth.metacode.validate.IsNotNull;
import com.github.brooth.metacode.validate.Validate;

/**
 * 
 */
public class ValidatorSample {

    public static class HireAction {
        @Validate(IsNotNull.class)
        public String name;
        @Validate(IsNotEmpty.class)
        public String[] degrees;
        @Validate(expression = "%m.age > 18")
        public int age;
        @Validate(expression = "%m.age - 18 >= %m.experience", expressionError = "too high experience")
        public int experience;

        public HireAction(String name, String[] degrees, int age, int experience) {
            this.name = name;
            this.degrees = degrees;
            this.age = age;
            this.experience = experience;
        }

        public void execute() {
            MetaHelper.validate(this);
        }
    }

    public static void main(String[] args) {
        new HireAction("John Smith", new String[]{"Master"}, 45, 15).execute();
    }
}
