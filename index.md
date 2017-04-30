---
layout: default
title: Java metacode generating framework
---

*Jeta* - is an Open Source framework, built on the top of `javax.annotation.processing`, that brings metaprogramming into *Java* Project. It aims to reduce boilerplate code and increase errors detection at compile-time.

Metaprogramming is achieved by code generating which makes programs fast and stable at runtime. The main goal is to ensure that if the metacode is compiled, it will work correctly. *Jeta* is also designed to provide rapid code.

If you are dissatisfied with `Java Reflection`, welcome aboard :)

<div class="alert alert-success" role="alert">
For android developers, <a href="/guide/androjeta/overview.html">Androjeta</a> is the better way to go.
</div>

At a glance:
--------
Even though *Jeta* is made to help *Java* developers to write source code generators, it also provides [a number](/guide.html) of features, built on the same foundation. Let's take a look on a simple example:

### @Log
Whatever logging tool is used in your project, the loggers can be supplied into classes through `Log` annotation. By default the logger has a name of the host (master) class:

    :::
    class LogSample {
        @Log
        Logger logger;
    }

instead of:

    :::
    class LogSample {
        private final Logger logger = LoggerFactory.getLogger(LogSample.class);
    }

The second approach implies copy-paste. Programmers often forget to replace the class, so loggers have incorrect names. In the first code snippet, there's no need to copy the logger code - it's easy to write, but even if you do, the `logger` will have the correct name.

Of course, this is a straightforward sample, but it illustrates what *Jeta* is all about - *less code, more stability*. Follow the [user's guide](/guide.html) to find more features and know how to create yours.

Installation (gradle):
----------------------

    :::
    repositories {
        jcenter()
    }

    dependencies {
        apt 'org.brooth.jeta:jeta-apt:2.3'
        compile 'org.brooth.jeta:jeta:2.3'
    }

Click [this page](/guide/install.html) for complete installation guide.


License
-------

    Copyright 2016 Oleg Khalidov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

