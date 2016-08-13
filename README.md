Jeta
====
`Jeta` - is an Open Source library, built on the top of `javax.annotation.processing`.

Tutorials:
--------------
Read the docs on [jeta.brooth.org](http://jeta.brooth.org).

Samples:
-------
[jeta-samples](https://github.com/brooth/jeta-samples)

Installation (gradle):
----------------------

```groovy
repositories {
    jcenter()
}

dependencies {
    apt 'org.brooth.jeta:jeta-apt:2.0'
    compile 'org.brooth.jeta:jeta:2.0'
}
```
[Here](https://plugins.gradle.org/search?term=apt) to can find `apt` plugins.

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
 
[jeta-samples]: https://github.com/brooth/jeta-samples
[observer-pattern]: https://en.wikipedia.org/wiki/Observer_pattern
[pubsub-pattern]: https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern
[di-pattern]: https://en.wikipedia.org/wiki/Dependency_injection
[apt-plugins]: https://plugins.gradle.org/search?term=apt
[androjeta]: https://github.com/brooth/androjeta
[jeta-configuration]: https://github.com/brooth/jeta#configuration
