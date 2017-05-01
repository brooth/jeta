<div class="page-header">
  <h2>How it works at Runtime</h2>
</div>

<img src="/static/images/at_runtime.png" width="700px"/>

### Master and Metacode
*Master* - is a Java type that uses an annotation and is processed by *Jeta*. For each master, *Jeta* generates a *Metacode* class. This class is located in the same package as its master and named as `<master name> + "_Metacode"`.

### Metasitory
*Metasitory* is a short for *Meta Code Repository*. This class holds all the required information about generated code. During the annotation processing, *Metasitory Writer* creates a meta storage. The storage contains the necessary information about the masters, their metacodes and annotations they use.

Currenlty *Jeta* provides `HashMapMetasitory` implementation. It uses `java.util.IdentityHashMap` for storing and querying the metacode. You can replace this implementation with any other. Follow [this guide](/guide/custom-metasitory.html) for complete instructions.

### Criteria
The *Criteria* provides a way to determine queries to metasitory. In most cases, *Criteria* is used for querying metacode by its master. It's allowed to query for a single instance, which generated for a given master, or for the all tree including the metacodes generated for parent masters as well. It gives you the ability to invoke a controller once in a base class and it will apply metacode to the child classes as well.

### Controller
*Controller* searches for the required metacode using *Criteria* and applies it to the *master*. Well, it's just a common case and some controllers do more complex work.

### Hello, World!
Let's continue to observe the *Hello, World!* example, from the [previous guide](/guide/code-generating.html), and find out how to apply the metacode to the sample class.

At first, we need to declare an interface that will be implemented by metacodes generated for `@SayHello` users (you might note this interface in the previous article):

    :::java
    public interface HelloWorldMetacode<M> {
        void setHelloWorld(M master);
    }

Secondly, create the controller:

    :::java
    public class HelloWorldController {
        protected Collection<HelloWorldMetacode> metacodes;

        public HelloWorldController(Metasitory metasitory, Object master) {
            Criteria criteria = new Criteria.Builder()
                .masterEqDeep(master.getClass())
                .usesAny(SayHello.class)
                .build();
            this.metacodes = (Collection<HelloWorldMetacode>) metasitory.search(criteria);
        }

        public void apply() {
            for (HelloWorldMetacode<Object> metacode : metacodes)
                metacode.setHelloWorld(master);
        }
    }

<span class="label label-info">Note</span> This controller creates a *Criteria* and searches for the metacode by itself for demonstration. The better way is to extend `MasterController` which does it for you.

Finally, we need to create a helper method in our *MetaHelper* class:

    :::java
    public static void setHelloWorld(Object master) {
        new HelloWorldController(metasitory, master).apply();
    }

What the *MetaHelper* is for and how to organize it in your project is explained in the [next article](/guide/meta-helper.html)

