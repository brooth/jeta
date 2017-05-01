<div class="page-header">
    <h2>Androjeta</h2>
</div>

In addition to [a number](/guide.html) of *Jeta* features, that are available on Android platform as well, *Androjeta* adds some extra. And of course, *Androjeta* follows the *Jeta* concepts - no reflection, compile-time validation and boilerplate-code elimination.

### `Java reflection` as a bad manner

Despite the fact that mobile phones might give odds to personal computers, it's still a bad manner nowadays to use *Java Reflection* in Android projects. Until the manufacturers offer the better batteries, the frameworks that are built on *Java Reflection*, won't be rivals to the ones built on `javax.annotation.processing` like *Jeta*.


### `onSaveInstanceState` issue

Every Android developer is probably familiar with `onSaveInstanceState` callback and knows about what the nightmare it might be to keep an activity in a state. You can read details on [developer.android.com](http://developer.android.com/training/basics/activity-lifecycle/recreating.html). Even though this approach requires a lot of boilerplate code, until now there wasn't a tool that could give you some help. Now *Androjeta* can:

    :::java
    @Retain
    String myVolatileString;

Go to [*Retain* guide](/guide/androjeta/retain.html) to find out how to make your life easier.

### FindView

*Androjeta* comes with an annotation that's going to be your favorite:

    :::java
    @FindView
    Button saveButton;

This feature eliminates `findViewById` usage. How is that possible? It's explained in [this article](/guide/androjeta/findviews.html).


### Jeta collectors vs scan packages

You aren't allowed to scan packages on Android if you need to search for some classes. Nevertheless, `Jeta Collectors` do that job much faster, even if it was possible on Android. Please, follow [this link](/guide/collector.html) to be aware of `Jeta Collectors` if you aren't yet.


###P.S.
In fact, *Jeta* was born as the result of all these issues, Android developers face to every day. Follow [this page](/guide.html) to be able to create stable Android apps and enjoy the process.
