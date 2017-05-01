<div class="page-header">
    <h2>Multi-Module Projects</h2>
</div>

Multi-module projects are the place where *Jeta* shows the best. You can share metacode between modules, override behavior, substitute classes and many more. It also should be important for any project, since projects are frequently composed of at least couple of modules - main and test. And the helper methods can be used in any of these modules.

As mentioned in [*Dependency Injection*](/guide/inject.html) article, you are allowed to extend injection providers. To make that possible, you must create one metacode-base for all your modules. In this tutorial, we will stick to the approach that we used in the previous guides - *MetaHelper*, which holds singleton instance of the metasitory.

To be able to add metacode from one module into another, e.g. from test to main one, metasitory provides a method for:

    :::java
    public interface Metasitory {
        void add(Metasitory other);
    }


Nevertheless, `MapMetasitory` provides a similar method that matches better for this case:

    :::java
    public class MapMetasitory implements Metasitory {
        public void loadContainer(String metaPackage) {...}
    }


The only thing we need to do is to pass our test package name into this method during initialization, something like:

    :::java
    public TestApp extends MainApp {
        protected void init() {
            super.init();
            MetaHelper.getMetasitory().loadContainer("com.example.test");
        }
    }

###DI Scopes

Let's go through an example, in which we need to substitute an entity that's injected in the main module. As it's described in [*Dependency Injection*](/guide/inject.html) guide, we must create a scope and provider that extends corresponding classes from the main module. With created scope you can provide different entities for old dependencies. There's one problem though - how to replace the scope with the test one? We can use `@Implementation` [feature](/guide/implementation.html) for:

    :::java
    @Scope
    @Implementation(AppScope.class)
    public class AppScope {
    }


Here, the implementation is provided by itself. In *MetaHelper* instead of creating `AppScope` we're looking for its implementation.

    :::java
    public class MetaHelper {
        private static MetaScope<AppScope> metaScope;

        private static init(Metasitory metasitory) {
            AppScope scope = new ImplementationController<>(metasitory, AppScope.class)
                    .getImplementation());
            metaScope = new MetaScopeController<>(metasitory, scope).get();
        }

        public static void inject(Object master) {
            new InjectController(metasitory, master).inject(metaScope);
        }
    }

Finally, in a test module we set a test scope as the implementation of the main scope but with higher priority:

    :::java
    @Scope
    @Implementation(value = AppScope.class, priority = 1)
    public class TestAppScope extends AppScope {
    }

After the test metacode is added into the main metasitory, the main injection scope will be replaced with the test one.

