Jeta
====
`Jeta` - is an Open Source library built on top of `javax.annotation.processing` that brings meta programming in your Java project. It aims to reduce boilerplate and increase error detection at compile-time.

Meta programming is achieved by code generation which makes programs fast and stable at runtime. The main goal is to ensure that the compiled metacode is the working code. In the other hand, `Jeta` was designed to provide generated code that is fast.

So if you are dissatisfied with `Java Reflection`, then you should give it a try :)

For android developers [Androjeta][androjeta] is recommended.

Usage:
--------
`Jeta` provides ready to use annotations that handle most frequently use cases. Custom annotations also available. See [jeta-samples project][jeta-samples] to find more information.

### @Log
Any logging tool can be provided via `@Log` annotation:
```java
class LogSample {
    @Log
    Logger logger;
}
```
instead of:
```java
class LogSample {
    private static final Logger logger = LoggerFactory.getLogger(LogSample.class);
}
```
The second  approach instigates copy-paste. The programmers often forget to replace the class name so many loggers have incorrect names.

### @Observer, @Subject, @Subscribe
Easy to use [observer pattens][observer-pattern]:
```java
class Observable {
    @Subject
    Observers<Event> observers;

    public Observable() {
        MetaHelper.createObservable(this);
    }

    private void fire(Event event) {
        observers.notify(event);
    }
}
```
```java
class Observer {
    Observable observable;

    public Observer() {
        observable = new Observable();
        MetaHelper.registerObserver(this, observable);
    }

    @Observer(Observable.class)
    void onEvent(Event event) {
        //...
    }
}
```

instead of:
```java
interface IObserver {
    void onEvent(Event event);
}
```
```java
class Observable {
    List<IObserver> observers;

    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    private void fire(Event event) {
        for(IObserver o : observers)
            o.onEvent(event);
    }
}
```
```java
class Observer implements IObserver {
    Observable observable;

    public Observer() {
        observable = new Observable();
        observable.addObserver(this);
    }

    @Override
    void onEvent(Event event) {
        //...
    }
}
```

Note that `Jeta` doesn't provide `MetaHelper` class. You should create your own helper class depending on your needs. You can use `MetaHelper` class from [jeta-samples][jeta-samples] as a prototype.

[Publish–subscribe (event bus) pattern] [pubsub-pattern] also available:

```java
class Publisher {
    private void fire(Message msg) {
        MetaHelper.getEventBus().publish(msg);
    }
}
```
```java
class Subscriber {
    public Subscriber() {
        MetaHelper.registerSubscriber(this);
    }

    @Subscribe
    protected void onMessage(Message msg) {
        //...
    }
}
```
See [jeta-samples project][jeta-samples] for additional event bus features.

### @Meta, @MetaEntity
`Jeta` provides [dependency injection][di-pattern] with some advantages. The dependencies can be extended in sub-modules (e.g. test module). It is possible to inject dependencies with parameters.
```java
@MetaEntity
class Producer {
    //...
}
```
```java
class Consumer {
    @Meta
    Producer producer;

    public Consumer() {
        MetaHelper.injectMeta(this);
    }
}
```

With parameters:
```java
@MetaEntity
class Producer {
    public Producer(String s, double d) {
        //...
    }
}
```
```java
class Consumer {
    @Meta
    MetaFactory factory;

    @Factory
    interface MetaFactory {
        Producer getProducer(String s, double d);
    }

    public Consumer() {
        MetaHelper.injectMeta(this);
        Producer p = factory.getProducer("abc", 0.75);
    }
}
```

In the test module `Producer` can be easily replaced with `TestProducer`:
```java
@MetaEntity(ext=Producer.class)
class TestProducer extends Producer {
    //...
}
```

You can use `@Inject` instead of `@Meta`. Add the option below to `jeta.properties` (see [Configuration][jeta-configuration]):
```properties
meta.alias=javax.inject.Inject
```

Static injection, providers, class injection and more in [jeta-samples][jeta-samples]

### @TypeCollector, @ObjectCollector
`Jeta` collectors assemble scattered classes:
```java
package com.example.collector.abc;

@Handler
class HandlerA {
    //...
}
```
```java
package com.example.collector.bcd.abc;

@Handler
class HandlerB {
    //...
}
```

All the handlers can be found:
```java
@TypeCollector(Handler.class)
class CollectorSample {
    void collectHandlers() {
        handlers = MetaHelper.collectTypes(CollectorSample.class, Handler.class);
    }
}
```

instead of:
```xml
<handlers>
    <handler class="com.example.collector.abc.HandlerA"/>
    <handler class="com.example.collector.bcd.abc.HandlerB"/>
</handlers>
```
```java
class CollectorSample {
    void collectHandlers() {
        // XML parsing, Class.forName(), etc
    }
}
```
In the second approach you have to remember to amend the xml file each time you create new handler. No validation also, so incorrect entered class name induces `ClassNotFoundException` at runtime.

`@ObjectCollector` also can be used to provide the instances of the collected objects. See [jeta-samples][jeta-samples] to find details.

### @Implementation
In a library or module, there might be a case when an implementation is unknown at compile-time and being defined in the end-product only:
```java
abstract class FooBuilder {
    public abstract Foo build();
}
```
```java
class FooFactory {
    public static Foo get() {
        FooBuilder builder = MetaHelper.getImplementation(FooBuilder.class);
        if (builder == null)
            throw new IllegalStateException("Foo builder not defined");

        return builder.build();
    }
}
```

In the end-product:
```java
@Implementation(FooBuilder.class)
class FooBuilderImpl extends FooBuilder {
    public Foo build() {
        //...
    }
}
```

### @Validate, @Singleton, @Multiton, @Proxy, Custom Annotations and more.
See [jeta-samples][jeta-samples] to find more.

Installation (gradle):
----------------------

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/brooth/maven"
    }
}

dependencies {
    apt 'org.brooth.jeta:jeta-apt:0.1-beta'
    compile 'org.brooth.jeta:jeta:0.1-beta'
}
```

Note that `Jeta` is a annotation processing tool so you need an `apt` plugin also:
[gradle apt plugins][apt-plugins]

Complete installation script and samples are available in [jeta-samples project][jeta-samples]

Configuration:
--------------
`Jeta` is configured via `jeta.properties` file (not required but recommended).
You should put this file in the root of the source directory. In this case you need to declare in your `build.gradle`:
```groovy
compileJava {
    options.sourcepath = files('src/main/java')
}
```

Other way to provide the path to your `jeta.properties` is annotation processor options. Some `apt` plugins provides the ability to declare those options:
```groovy
apt {
    arguments {
        jetaProperties "$project.projectDir/src/main/java/jeta.properties"
    }
}
```

#### jeta.properties:
It's highly recommended to define `metasitory package` (project's package). This package is used by `Jeta` to generate `metasitory` class.
```properties
metasitory.package=com.company.project
```

If you need to keep some annotations without processing you can turn their processors off (`reg-exp` is allowed in the annotation names):
```properties
processors.disable=Meta.*,Log
```

To speed up annotation processing, it's recommended to turn `utd` feature on. This option says to `Jeta` to use already generated code in case the source code hasn't been changed:
```properties
utd.enable=true

# delete generated file if its source file has been removed
utd.cleanup=true

# absolute or relative `jeta.properties' file path where utd data is stored
utd.dir=../../../build/jeta-utd-files

# output info about utd file state
# + added or replaced
# - removed
# * up-to-date
debug.utd_states=true
```

Output debug information:
```properties
debug=true
debug.built_time=true
```
More options can be found in [jeta-samples project][jeta-samples]

License
-------

    Copyright 2015 Oleg Khalidov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
[jeta-samples]: https://github.com/brooth/jeta-samples
[observer-pattern]: https://en.wikipedia.org/wiki/Observer_pattern
[pubsub-pattern]: https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern
[di-pattern]: https://en.wikipedia.org/wiki/Dependency_injection
[apt-plugins]: https://plugins.gradle.org/search?term=apt
[androjeta]: https://github.com/brooth/androjeta
[jeta-configuration]: https://github.com/brooth/jeta#configuration
