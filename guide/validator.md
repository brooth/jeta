<div class="page-header">
    <h2>Validators</h2>
</div>

*Jeta* provides a rich validation framework. Let's find out why it's better than others. For this example, let's assume we have an action that hires employees for a job.

    :::java
    public class HireAction {
        @Validate(NotBlank.class)
        String name;
        @Validate(NotEmpty.class)
        String[] degrees;

        public HireAction(String name, String... degrees) {
            this.name = name;
            this.degrees = degrees;
        }

        public void hire() {
            MetaHelper.validate(this);
            //...
        }

        public List<String> checkMePlease() {
            return MetaHelper.validateSafe(this);
        }
    }

Well, it's clear enough that `NotBlank` validator checks a string isn't blank. `NotEmpty` can check an array, collection, map or string isn't empty. `MetaHelper.validate()` throws `ValidationException` in case of validation error, `MetaHelper.validateSafe()` returns the errors as a list.

*Jeta* comes with predefined validators - `NotBlank`, `NotEmpty`, and `NotNull`. But, of course, you can create as many as you need. For the illustration here's the listing of `NotNull` so you can write similar ones:

    :::java
    public class NotNull implements Validator<Object, Object> {
        private String fieldName;

        @Override
        public boolean validate(Object master, Object field, String fieldName) {
            this.fieldName = fieldName;
            return field != null;
        }

        @Override
        public String describeError() {
            return fieldName + " is null";
        }
    }

###MetaValidator

*MetaValidator* allows to create more complex validators. Let's say we need to access the master fields, e.g. to calcualte the sum of two of them. Of course, we can create an interface, declare getters for these fields, implement this interface and so on, but it's simpler to use *MetaValidator* instead:

    :::java
    @MetaValidator(
            emitExpression = "$f > 18",
            emitError = "Too young"
    )
    public interface AgeValidator extends Validator {}

<span/>

    :::java
    @MetaValidator(
            emitExpression = "$f <= $m.age - 18",
            emitError = "${$f} years of experience " +
                    "is too high for the age of ${$m.age}"
    )
    public interface ExperienceValidator extends Validator {}

<span/>

    :::java
    public class HireAction {
        @Validate(NotBlank.class)
        String name;
        @Validate(NotEmpty.class)
        String[] degrees;
        @Validate(AgeValidator.class)
        int age;
        @Validate(ExperienceValidator.class)
        int experience;

        public HireAction(String name, int age, int experience, String... degrees) {
            this.name = name;
            this.degrees = degrees;
            this.age = age;
            this.experience = experience;
        }

        public void hire() {
            MetaHelper.validate(this);
        }

        public List<String> checkMePlease() {
            return MetaHelper.validateSafe(this);
        }
    }

As you probably noticed, `ExperienceValidator` uses `age` field for the check. Besides, if you use `AgeValidator` on a string field, it will fail during compilation. How does it work? Well, `$f` will be replaced with the field it's applied to. `$m` refers to the master instance. In `${}` you can write any Java code you want to. Before the compilation *Jeta* will create the source code by these strings, which in case of misspelling, of course, won't be assembled.

###ValidatorAlias

*ValidatorAlias* allows you to create custom annotations that you can use as validators themself. You need to define these annotations in `jeta.properties` file. Please, read [this post](/guide/config.html) if you have questions about this file.

    :::properties
    validator.alias.com.example.NotYoung = com.example.AgeValidator
    validator.alias.com.example.NotCheater = com.example.ExperienceValidator

<span class="label label-info">Note</span> You must use the `validator.alias.` prefix before the annotaions.

Certainly *Jeta* provides aliases for its validators. Look them up in `org.brooth.jeta.validate.alias` package.

Well, due to *ValidatorAlias* the code looks cleaner:

    :::java
    public class HireAction {
        @NotBlank
        String name;
        @NotEmpty
        String[] degrees;
        @NotYoung
        int age;
        @NotCheater
        int experience;

        public HireAction(String name, int age, int experience, String... degrees) {
            this.name = name;
            this.degrees = degrees;
            this.age = age;
            this.experience = experience;
        }

        public void hire() {
            MetaHelper.validate(this);
        }

        public List<String> checkMePlease() {
            return MetaHelper.validateSafe(this);
        }
    }

<span class="label label-warning">Pay attention</span> Prior to version 2.0 the alias-annotations must be compiled into byte-code in order to use them as the aliases.

<span class="label label-success">Tip</span> You can use *Jeta* `NonNull` validator alongside with `javax.annotation.Nonnull`. It allows you to actually validate *NPE* issues, not just highlight them by an IDE.

    :::properties
    validator.alias.javax.annotation.Nonnull = org.brooth.jeta.validate.NotNull

###MetaHelper

The validation helper methods would be:

    :::java
    public static void validate(Object master) throws ValidationException {
        new ValidationController(metasitory, master).validate();
    }

    public static List<String> validateSafe(Object master) {
        new ValidationController(metasitory, master).validateSafe();
    }

