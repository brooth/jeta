<div class="page-header">
    <h2>Installation</h2>
</div>


*Androjeta* installation process is similar to the [installation](/guide/install.html) of *Jeta*. But here it's recommended to use [android-apt plugin by Hugo Visser](https://bitbucket.org/hvisser/android-apt) as the plugin.

Here's the complete listing:

    :::groovy
    buildscript {
        repositories {
            mavenCentral()
        }

        dependencies {
            classpath 'com.neenbedankt.gradle.plugins:android-apt:+'
        }
    }

    apply plugin: 'android-apt'

    repositories {
        jcenter()
    }

    apt {
        arguments {
            jetaProperties "$project.projectDir/src/main/java/jeta.properties"
        }
    }

    dependencies {
        apt 'org.brooth.androjeta:androjeta-apt:2.3'
        compile 'org.brooth.androjeta:androjeta:2.3'
    }


Go to the [configuration guide](/guide/config.html) to know about what are `apt-arguments` needed for.

