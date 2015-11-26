/*
 * Copyright 2015 Oleg Khalidov
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
 */

package org.javameta.samples.validate;

import org.javameta.samples.MetaHelper;
import org.javameta.validate.*;

import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ValidatorSample {

    @MetaValidator(
            emitExpression = "$m.age > 18",
            emitError = "${I18n.get(\"TOO_YOUNG\", \"en\")}"
    )
    public interface AgeValidator extends Validator {}

    @MetaValidator(
            emitExpression = "$m.age - 18 >= $m.experience",
            emitError = "${$m.experience} years of experience is too high for the age of ${$m.experience}"
    )
    public interface ExperienceValidator extends Validator {}

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
            System.out.println(name + ", see you monday!");
        }

        public void checkIsAppropriate() {
            List<String> incompatibilities = MetaHelper.validateSafe(this);
            if (incompatibilities.size() > 0) {
                System.out.println("Oops, ");
                for (String incompatibility : incompatibilities) {
                    System.out.println(incompatibility);
                }
            }
        }
    }

    public static void main(String[] args) {
        new HireAction("John Smith", 45, 15, "Master Chef").execute();
        new HireAction(null, 17, 15).checkIsAppropriate();
    }
}
