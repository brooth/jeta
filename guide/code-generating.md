<div class="page-header">
  <h2>Code generating</h2>
</div>

### `javax.annotation.processing`

It's good to know: *Jeta* isn't evaluating the annotations by using *Reflection API* at runtime. It generates all the necessary code at compile-time. It allows to emit metacode as it's handwritten. Also, unlike reflection, it's possible to check the code for errors before it's launched.


### How it works:
There's a bit of information on the Internet, but `javac` allows us to generate source code. Moreover, it's available since Java <span class="label label-info">1.5</span>. It can launch an [Annotation Processor](https://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html) before your Java sources will be compiled, so you can scan and process all the annotations in your code.

<div class="alert alert-warning" role="alert">
    Currenty jeta is tested on Java 1.7. Older version might be supported in future releases.
</div>

<span class="label label-success">Thanks to</span> [JavaPoet by Square](https://github.com/square/javapoet) for the great framework that *Jeta* uses to write Java code.

### Hello, World!<a name="HelloWorldSample"></a>
Let's take a look at the example, in which `@SayHello` annotation sets *"Hello, World!"* into the field.

    :::java
    public @interface SayHello {
    }

    public class HelloWorldSample {
        @SayHello
        String str;
    }

If *Jeta* had a processor for this example, the metacode would be:

    :::java
    public class HelloWorldSample_Metacode implements HelloWorldMetacode<HelloWorldSample> {
        @Override
        public void setHelloWorld(HelloWorldSample master) {
            master.str = "Hello, World!";
        }
    }

You can find sample code of such processor on [this page](/guide/custom-processor.html). Also, there's a similar example on [GitHub](https://github.com/brooth/jeta-samples).

How `HelloWorldSample_Metacode` is applied to `HelloWorldSample` is explained in the [next article](/guide/at-runtime.html).
