<div class="page-header">
  <h2>Installation</h2>
</div>

In this guide, we are going to see how to install *Jeta* using `gradle` build system. Currently, `jCenter` repository is the way to get the artifacts. It may be available on `maven` repository in a future release.

Add next lines into your `build.gradle`:

    :::groovy
    repositories {
        jcenter()
    }

    dependencies {
        apt 'org.brooth.jeta:jeta-apt:2.3'
        compile 'org.brooth.jeta:jeta:2.3'
    }


In order to use `apt`, you need to install an appropriate plugin as well. Follow [gradle apt plugins](https://plugins.gradle.org/search?term=apt) page to find out the options. For this example let's choose `net.ltgt.apt`:

    :::groovy
    buildscript {
        repositories {
            maven {
                url 'https://plugins.gradle.org/m2/'
            }
        }

        dependencies {
            classpath 'net.ltgt.gradle:gradle-apt-plugin:0.5'
        }
    }

    apply plugin: 'net.ltgt.apt'


After the plugin is applied, you can use `apt` to setup the annotation processing environment.

### `jeta.properties`
In the next step, it's recommended to create `jeta.properties` file to be able to config code generation behavior. You should put this file into root package of the source set. Complete configuration guide is on [this page](/guide/config.html). At this step - to provide `metasitory.package` property is enough:

    :::properties
    metasitory.package=com.example


<span class="label label-info">Note</span> `metasitory.package` has to be unique for any module. `MapMetasitoryWriter` uses this package to generate metasitory containers in it. Follow [this guide](/guide/at-runtime.html) to get information about metasitories.

To allow `jeta.properties` to be found by *Jeta* one extra step is needed. In case you put this file in the root package, you can provide the `sourcepath` by adding next snippet into your `build.gradle`:

    :::groovy
    compileJava {
        options.sourcepath = files('src/main/java')
    }

For a reason *Gradle* doesn't provide it by itself.

Another available option - `jetaProperties` apt argument. Some of the plugins support providing such arguments (`net.ltgt.apt` currently doesn't)

    :::groovy
    apt {
        arguments {
            jetaProperties "$project.projectDir/src/main/java/jeta.properties"
        }
    }

<div class="alert alert-success" role="alert">
For android projects <a href="https://bitbucket.org/hvisser/android-apt">android-apt plugin by Hugo Visser</a> is recommended.
</div>

Complete `build.gradle`:

    :::groovy
    buildscript {
        repositories {
            maven {
                url 'https://plugins.gradle.org/m2/'
            }
        }
        dependencies {
            classpath 'net.ltgt.gradle:gradle-apt-plugin:+'
        }
    }

    apply plugin: 'net.ltgt.apt'

    repositories {
        jcenter()
    }

    compileJava {
        options.sourcepath = files('src/main/java')
    }

    dependencies {
        apt 'org.brooth.jeta:jeta-apt:2.3'
        compile 'org.brooth.jeta:jeta:2.3'
    }

At this point, you can start using *Jeta*, but properly configured, it's the better way to go. Refer to the [next guide](/guide/config.html) to find out details.
