<div class="page-header">
<h2>Configuration</h2>
</div>

In order to configure *Jeta* you need to create `jeta.properties` file in the root package of the source set. It's a plain *Java Properties* file with `key=value` format.
Unfortunately, there's no way of knowning where the source directory is located, so you must provide it by yourself. There are two ways to achieve this. First one is to declare `sourcepath` option in your `build.gradle`:

### `build.gradle`

    :::groovy
    compileJava {
        options.sourcepath = files('src/main/java')
    }

The second way is available using `apt` arguments. Note that you need a plugin that supports this feature. If not, there are some hacks that might help, so you should google it. If your plugin allows, add next snippet into the `build.gradle` file:

    :::groovy
    apt {
        arguments {
            jetaProperties "$project.projectDir/src/main/java/jeta.properties"
        }
    }

### `jeta.properties`
Let's go through the options available to controll code generating. As previously mentioned, the most required property is `metasitory.package`. It must be unique for any module, e.g. test module. You will need this package to provide to `MapMetasitory` constructor at runtime to use this metasitory. The other options are described below as comments:

    :::properties
    # Source directory path. Absolute or relative to `jeta.properties` path
    # to the source folder. Define it in case `jeta.properties` isn't in the root
    # of the source set. (`.` by default)
    sourcepath=.

    # Unique per module java package, where metasitory will be stored.
    # (`` by default, recommended to define)
    metasitory.package=com.example

    # Set this option to `true` to skip code re-generating in case the master's source code
    # hasn't been changed since the previous run. Boost up code processing.
    # (`false` by default)
    utd.enable=true

    # Delete metacode if its master does not exists anymore
    # (`true` by default if `utd` feature's enabled)
    utd.cleanup=true

    # Absolute or relative to `jeta.properties` path, where `utd` data is stored
    # ('java.io.tmpdir' by default, recommended to define)
    utd.dir=../../../build/jeta-utd-files

    # Output debug information (false by default)
    debug=true

    # Output the time metacode built in (true by default)
    debug.built_time=true

    # Output `utd` statuses:
    #  `+` - metacode created or rewritten
    #  `-` - metacode removed (if `utd.cleanup` enabled)
    #  `*` - master is up-to-date
    # (`true` by default if `debug` and `utd` are enabled)
    debug.utd_states=true

    # Custome annotation processors (comma separated)
    # go to http://jeta.brooth.org/guide/custom-processor
    processors.add=com.example.apt.MyCustomProcessor

    # Disable a processor by its annotation (comma separated, reg-exp allowed)
    processors.disable=^Meta.*$,^Log$

    # Metacode file header
    file.comment=\n Use is subject to license terms. \n

    # Default scope for dependency injection
    # go to http://jeta.brooth.org/guide/inject
    inject.scope.default = org.brooth.jeta.tests.inject.DefaultScope

    # org.brooth.jeta.inject.Inject annotation alias.
    inject.alias=javax.inject.Inject

    # org.brooth.jeta.Provider alias
    inject.alias.provider=javax.inject.Provider

    # Custom metasitory writer
    # go to http://jeta.brooth.org/guide/custom-metasitory
    metasitory.writer=org.brooth.jeta.apt.EchoMetasitoryWriter

