<div class="page-header">
    <h2>Event bus</h2>
</div>

*Jeta* providers an implementation of [*Publish-Subscribe* pattern](https://en.wikipedia.org/wiki/Publish-subscribe_pattern). Besides basic features, it has a great advantage - no reflection is used at all. Excited? Let's go through.

###Subscriber

    :::java
    class Subscriber {
        private SubscriptionHandler handler;

        public Subscriber() {
            handler = MetaHelper.registerSubscriber(this);
        }

        @Subscribe
        protected void onMessage(Message msg) {
        }
    }

As well as [observer's handler](/guide/observer.html), `SubscriptionHandler` lets you control subscription workflow. You can stop listening to a message:

    :::java
    handler.unregister(Message.class);

or all of them:

    :::java
    handler.unregisterAll();

### Priority

You're able to order the handlers by priority. Handlers with higher priority will be invoked first:

    :::java
    @Subscribe(priority = 146)
    protected void onMessage(Message msg) {
    }

### Filters

*Jeta Event-Bus* supports filters, so you can reject unwanted messages:

    :::java
    @Subscribe(filters = { OddFilter.class })
    protected void onMessage(Message msg) {
    }

For the illustration, `OddFilter` code is:

    :::java
    public class OddFilter implements Filter<Object, MyMessage> {
        public boolean accepts(Object master, String methodName, MyMessage msg) {
            return msg.id() % 2 != 0;
        }
    }

Two filters are available out of the box, by `id` and by `topic`. You can define them via `@Subscribe` annotation:

    :::java
    @Subscribe(id = {2, 4}, topic = {"two", "four"})
    protected void onMessage(Message msg) {
    }

<span class="label label-info">Note</span> To make `id` and `topic` filters possible, all messages must implement `org.brooth.jeta.eventbus.Message` interface or extend `org.brooth.jeta.eventbus.BaseMessage` class.


### MetaFilters

As previously mentioned, *Jeta* is designed to detect as many errors at compile-time as possible . With plain filters this principle works via generic types. Let's say we have a filter that works with a particular message type:

    :::java
    public class MyMessage extends BaseMessage {
    }

    public class MyFilter implements Filter<Object, MyMessage> {
        public boolean accepts(Object master, String methodName, Message msg) {
            return true;
        }
    }

If you try to use this filter on a method with a parameter which not assigneble from `MyMessage` class, the code won't be compiled, but *MetaFilters* allow you to write more complex checks, e.g. access to a nonprivate constant:

    :::java
    @MetaFilter(emitExpression = "$m.THE_NUMBER % 2 == 0")
    public interface EvenMetaFilter extends Filter {}


Here `$m` will be replaced with the master class in which this filter is used. So if the master doesn't have field `THE_NUMBER`, you will get the error during compilation. You can also use `$e` to get access to the message instance.


### MetaHelper

You can either use *Jeta* basic implementation of `EventBus` - `org.brooth.jeta.eventbus.BaseEventBus` or implement your own. Also, you can use a single instance of the bus or create many. Nevertheless, you must pass an instance of the bus to the controller. Let's create a helper method for:

    :::java
    public static SubscriptionHandler registerSubscriber(Object master) {
        return new SubscriberController<>(metasitory, master).registerSubscriber(bus);
    }

You definitely should follow [this link](/guide/meta-helper.html) if you are still not familiar with *MetaHelper*.
