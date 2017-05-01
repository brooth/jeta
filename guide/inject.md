<div class="page-header">
    <h2>Dependency Injection</h2>
</div>

*Dependency injection* is the most powerful part of *Jeta*. In addition to the common `DI` functions it offers some extras:

* Inject entities with parameters
* Extend the entities

###MetaHelper

Before we start, let's define a helper method we're going to inject with:

    :::java
    public static void inject(Object master) {
        new InjectController(metasitory, master).inject(scope);
    }


Please, read [this article](/guide/meta-helper.html) first, if you have questions about *MetaHelper*.

<a name="Producer"></a>
### Producer

Let's start with a straightforward example:

    :::java
    @Producer
    class MyProducer {
    }

    class MyConsumer {
        @Inject
        MyProducer producer;

        public MyConsumer() {
            MetaHelper.inject(this);
        }
    }

Well, the simplest way to inject an entity is to define this entity as `Producer`. Everytime  `inject()` method is invoked, new instance of *MyProducer* will be injected. If you need a single instance of an entity, set up `singleton` flag to `true`: `@Producer(singleton=true)`. <span class="label label-info">Note</span> It makes a singleton per `Scope`. Read below about the [scopes](#Scopes) .

In the snippet above, the injector creates an instance of *MyProducer* through default constructor. It's allowed to create a static factory method and provide instances via it:

    :::java
    @Producer(staticConstructor = "newInstance")
    class MyProducer {
        public static MyProducer newInstance() {
            return new MyProducer();
        }
    }

<span class="label label-info">Note</span> You shouldn't distrust this approach. In case of misspelling, it fails at compile-time.

<br/>
Besides straight injection, you can provide it using `Lazy`, `Provider` or inject its `class`:

    :::java
    @Inject
    Provider<MyProducer> producerProvider;
    @Inject
    Lazy<MyProducer> lazyProducer;
    @Inject
    Class<? extends MyProducer> producerClass;

<span class="label label-info">Note</span> *Lazy* class provides a single instance of an entity and initializes it on demand. *Provider* creates new instance each invocation.

`Class<? extends >` is useful for a multi-module project where an entity can be [extended](#Extending) by another.

As previously written, *Jeta DI* allows you to inject entities with parameters:

    :::java
    @Producer
    class MyProducer {
        public MyProducer(String s, double d) {
        }
    }

    class MyConsumer {
        @Inject
        MyFactory factory;
        @Factory
        interface MyFactory {
            MyProducer getMyProducer(String s, double d);
        }

        public MyConsumer() {
            MetaHelper.inject(this);
            MyProducer p = factory.getMyProducer("abc", 0.75);
        }
    }

<span class="label label-info">Note</span> Once you defined a `Factory`, you can use it for the all dependencies of a master, including entities without parameters, provided via `Lazy`, `Provider` and `Class`.


<div class="alert alert-success" role="alert">
Note that the injection expression must match the entity construction, i.e. you can't inject an entity without the factory if there's no empty constructor for it, otherwise the code won't be compiled. This aspect makes <code>Jeta DI</code> more helpful for development. If the code is compiled, it should work properly.
</div>


### Entity Provider
In case you need a provider of an entity, e.g. to be able to inject thirdparty classes, you need to annotate this provider with `@Producer(of=MyProducer.class)`:

    :::java
    @Producer(of = MyProducer.class)
    class MyProducerProvider {
        @Constructor
        public MyProducer get() {
            return new MyProducer();
        }
    }

<span class="label label-info">Note</span> In case of entity provider you must explicitly define factory methods via `@Constructor` annotation. Also, `staticConstructor` is used to create the provider, not entities. If it's necessary to create entities via static factory method, put `@Constructor` annotation on it:

    :::java
    @Producer(of = MyProducer.class)
    class MyProducerProvider {
        @Constructor
        public static MyProducer getMyProducer() {
            return new MyProducer();
        }
    }

<a name="Scopes"></a>
### Scopes

You can define as many scopes as needed. However, there must be at least one *Scope* in order to inject module's entities. So, it's important to understand that no entity can be provided outside a scope. On the other hand, one entity can belong to many scopes.

In general, you must specify the scope when you're creating an entity:

    :::java
    @Producer(scope = MyScope.class)
    class MyProducer {
    }

but it's allowed to define a default scope. In this case, you can leave `scope` argument empty. To set a scope as default, add this option in your `jeta.properties`:

    :::properties
    inject.scope.default = com.extample.MyScope

Refer to [configuration guide](/guide/config.html) if you have any questions about `jeta.properties`.

Let's create a scope and go through its details.

    :::java
    @Scope
    public class MyScope {
    }

In fact, this class does nothing, but is used to create a `MetaScope`:

    :::java
    MetaScope<MyScope> myMetaScope =
        new MetaScopeController<>(metasitory, new MyScope()).get();

<span class="label label-info">Note</span> *MetaScope* is the major class of *Jeta DI*. It holds the  information about providers and satisfies module's dependencies. You need to pass a *MetaScope* to `InjectController` in order to inject:

    :::java
    new InjectController(metasitory, master).inject(myMetaScope);

<span class="label label-info">Note</span> Use `StaticInjectController` for static dependencies.

Nevertheless, there's a little trick that makes scopes useful as well. You can use them to pass data to the providers. Let's say we need an object `Application` in our entities. In this case, the better way is to use a scope for:

    :::java
    public class MyScope {
        private Application app;

        public MyScope(Application app) {
            this.app = app;
        }

        public Application getApplication() {
            return app;
        }
    }

<span/>

    :::java
    MetaScope<MyScope> myMetaScope =
        new MetaScopeController<>(metasitory, new MyScope(application)).get();


Now, you can access the `Application` instance inside an entity:

    :::java
    @Produce
    class MyProducer {
        private Application app;

        class MyProducer(MyScope __scope__) {
            this.app = __scope__.getApplication();
        }
    }

<span class="label label-warning">Pay attension</span> You must precisely name constructor parameter as `__scope__`.


### Module

*Module* is the entry point to the *Jeta DI* configuration of your project. In the foreground it's just used to define the scopes, but in the background, it does more complex work.

    :::java
    @Module(scopes = { MyScope.class })
    class MyModule {
    }

<span class="label label-warning">Important</span> You must define exact one `@Module` for a module in which you want to use *Jeta DI*. Otherwise you get compilation error.

### Aliases

*Jeta* brings all the required annotations to inject right away. You can find those in `org.brooth.jeta.inject` package. Nevertheless, it's allowed to use thirdparty annotations as the aliases. It might be useful for the projects that already use another DI framework.

Let's say you want to use `javax.inject.Inject` to supply entities. To do so, append these lines in your `jeta.properties`:

    :::properties
    inject.alias = javax.inject.Inject
    # provider as well
    inject.alias.provider = javax.inject.Provider

<span class="label label-warning">Pay attention</span> To let *InjectController (StaticInjectController)* know about `javax.inject.Inject` you also have to pass this annotation to the constructors, otherwise the controllers won't find the masters that use those annotations.

    :::java
    new InjectController(metasitory, master, javax.inject.Inject.class)
        .inject(myMetaScope);

### Hello, World!

For the demonstration let's change the [Hello, World!](/guide/code-generating.html#HelloWorldSample) example. Instead of providing *Hello, World!* via `@SayHello` annotation, we'll inject it.

First, we need to create *Module* and *Scope*:

    :::java
    @Scope
    public class HelloWorldScope {}

    @Module(scopes = HelloWorldScope.class)
    interface HelloWorldModule {}

Next, create a `String` provider:

    :::java
    @Producer(of = String.class, scope = HelloWorldScope.class)
    public class StringProvider {
        @Constructor
        public static String get() {
            return "Hello, World!";
        }
    }

And the helper method:

    :::java
    public static void inject(Object master) {
        MetaScope<HelloWorldScope> metaScope =
            new MetaScopeController<>(metasitory, new HelloWorldScope()).get();
        new InjectController(metasitory, master).inject(metaScope);
    }

Finally, modify the sample:

    :::java
    public class HelloWorldSample {
        @Inject
        String str;

        public HelloWorldSample() {
            MetaHelper.inject(this);
        }
    }


<a name="Extending"></a>
### Extending

As noticed above, *Jeta DI* lets you extend entities easily. For example, we'll modify the *Hello, World!* example above. Instead of *"Hello, World!"* let's inject *"Hello, Universe!"*.

First, we need to define new scope and provider:

    :::java
    @Scope(ext = HelloWorldScope.class)
    public class HelloUniverseScope extends HelloWorldScope {
    }

    @Producer(of = String.class, scope = HelloUniverseScope.class)
    public class StringProviderExt {
        @Constructor
        public static String get() {
            return "Hello, Universe!";
        }
    }

Also, change the scope from `HelloWorldScope` to `HelloUniverseScope` in the helper:

    :::java
    public static void inject(Object master) {
        MetaScope<HelloWorldScope> metaScope =
            new MetaScopeController<>(metasitory, new HelloUniverseScope()).get();
        new InjectController(metasitory, master).inject(metaScope);
    }

At this point, *Jeta DI* will inject *"Hello, Universe!"* into `str` as you expect. But, let's clarify what's happening in these listings.

`@Scope(ext)` - allows you to use a scope as the one it extends from. So, we can provide *"Hello, World!"* string using `MetaScope<HelloUniverseScope>` in our example as well. Nevertheless, the real benefit is that you can replace the providers from super scope with new ones. For this purpose we created `StringProviderExt` and pointed its scope to `HelloUniverseScope`.

Apart from direct extending, it's allowed to substitude the super entities as well. To illustrate that, let's extend our [`MyProducer`](#Producer) entity:

    :::java
    @Producer(ext = MyProducer.class)
    public class MyTestProducer extends MyProducer {
    }

Now, in addition to being able to inject `MyTestProducer`, you can also satisfy `MyProducer` dependencies.

<span class="label label-warning">Important</span> The scope of the extender entity must extend the scope of the extending entity as well. This means that for our `MyTestProducer` we must create a scope that extends [`MyScope`](#Scopes):

    :::java
    @Scope(ext = MyScope.class)
    public class MyTestScope {
    }

    @Producer(ext = MyProducer.class, scope = MyTestScope.class)
    public class MyTestProducer extends MyProducer {
    }

Besides this article, it's recommended to go through [multi-modules projects](/guide/multi-modules.html) guide. It describes how to share the metacode between modules.

