<div class="page-header">
  <h2>Meta Helper</h2>
</div>

*MetaHelper* is a class not provided by *Jeta*. It's just a way to organize metacode invocation in your project. If you aren't comfortable with static helpers, you shouldn't use it in your project. This class is used as an entry point to the framework's features. You can hold a singleton of the metasitory in it and pass it to the controllers:

    :::java
    public class MetaHelper {
        private static final MetaHelper instance = new MetaHelper("com.example");
        private final Metasitory metasitory;

        private MetaHelper(String metaPackage) {
            metasitory = new MapMetasitory(metaPackage);
        }
    }


 You should define the static helpers on demand. Let's add one that creates loggers for elements annotated with `@Log`:

    :::java
    public class MetaHelper {
        private final NamedLoggerProvider<Logger> loggerProvider;

        private MetaHelper(String metaPackage) {
            loggerProvider = new NamedLoggerProvider<Logger>() {
                public Logger get(String name) {
                    return new Logger(name);
                }
            };
        }

        public static void createLoggers(Object master) {
            new LogController(instance.metasitory, master)
                .createLoggers(instance.loggerProvider);
        }
    }

Once you defined `createLoggers()`, you can supply loggers into your classes:

    :::java
    public class MyBaseClass {
        public MyBaseClass() {
            MetaHelper.createLoggers(this);
        }
    }

 <span class="label label-info">Note</span> `LogController` supports deep metacode invocation. This means that all extended from `MyBaseClass` classes will get their loggers as well.

    :::java
    public class MyCompleteClass extends MyBaseClass {
        @Log
        Logger logger;

        public MyCompleteClass() {
            logger.info("it works!");
        }
    }


### Hello, World!
Let's complete our `HelloWorld` example from the [previous guide](/guide/at-runtime.html). Its *MetaHelper*:

    :::java
    public class MetaHelper {
        private static final MetaHelper instance = new MetaHelper("com.example");
        private final Metasitory metasitory;

        private MetaHelper(String metaPackage) {
            metasitory = new MapMetasitory(metaPackage);
        }

        public static void setHelloWorld(Object master) {
            new HelloWorldController(instance.metasitory, master).apply();
        }
    }


