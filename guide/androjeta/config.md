---
title: Androjeta - Configuration
is_androjeta: true
---

<div class="page-header">
    <h2>Configuration</h2>
</div>

In addition to the *Jeta* [properties]({{ site.baseurl }}/guide/config.html) you must define your project `applicationId` in order to use [*FindView*]({{ site.baseurl }}/guide/androjeta/findviews.html), [*OnClick*]({{ site.baseurl }}/guide/androjeta/clicks.html) or [*OnLongClick*]({{ site.baseurl }}/guide/androjeta/clicks.html). Well, if you haven't read [how to config *Jeta*]({{ site.baseurl }}/guide/config.html) yet, you should do it first.

To define module `applicationId` add this line in your `jeta.properties`:

```properties
application.package = <your applicationId here>
```
