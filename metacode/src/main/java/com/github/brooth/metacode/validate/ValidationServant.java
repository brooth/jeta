package com.github.brooth.metacode.validate;

import com.github.brooth.metacode.MasterMetacode;
import com.github.brooth.metacode.MasterServant;
import com.github.brooth.metacode.metasitory.Metasitory;

/**
 * 
 */
public class ValidationServant extends MasterServant<Object, ValidationServant.ValidatorMetacode<Object>> {

    protected ValidationServant(Metasitory metasitory, Object master) {
        super(metasitory, master, Validate.class);
    }

    public void validate() {
        for (ValidatorMetacode<Object> metacode : metacodes)
            metacode.validate(master);
    }

    public interface ValidatorMetacode<M> extends MasterMetacode<M> {
        void validate(M master);
    }

    public class HireAction {
        @Validate(NotNull.class)
        public String name;
        @Validate(NotEmpty.class)
        public String[] deggries;
        @Validate(expression = "age > 18")
        public int age;
        @Validate(expression = "age - 18 >= expirience", expressionError = "too high expirience")
		public int expirience;

        public void execute() {
            new ValidationServant(null, this).validate();
        }
    }
}
