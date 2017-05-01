<div class="page-header">
    <h2>Implementation</h2>
</div>

In a multi-module project you may have a case when a feature can to be implemented in many ways, e.g. *DbManger* needs a driver to work with a database. It might be `sqlite`, `postgres` or any other. *DbManger* does a lot - supports JPQL, mappings, DAO and other cool features. However, it depends on the driver that allows it to communicate with the exact DB implementation. Let's see how *Jeta Implementation* can help:

    :::java
    class DbManger {
        DbDriver driver;

        public DbManger() {
            driver = MetaHelper.getImplementation(DbDriver.class);
            if(driver == null)
                throw new IllegalStateException("Db driver not defined");
        }
    }

    interface DbDriver {
        // methods to work with a db...
    }

In a module, where we want to use `DbManger`, we can provide an implementation:

    :::java
    @Implementation(DbDriver.class)
    class SqliteDbDriver implements DbDriver {
        //...
    }

Furthermore, in the test module we can substitute the driver with a fake one:

    :::java
    @Implementation(DbDriver.class, priority = 10)
    class TestDbDriver implements DbDriver {
        //...
    }

###MetaHelper

The helper method would be:

    :::java
    public static <I> getImplementation(Class<I> of) {
        return new ImplementationController<I>(metasitory, of).getImplementation();
    }

In addition to `getImplementation()`, *ImplementationController* provides `getImplementations()` method, if it's necessary to get all implementations, and `hasImplementation()` to check if there is any.

