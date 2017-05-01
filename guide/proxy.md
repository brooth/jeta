<div class="page-header">
    <h2>Proxy</h2>
</div>

*Jeta Proxy* allows you to create [proxy objects](https://en.wikipedia.org/wiki/Proxy_pattern). Let's demonstrate this feature with an example.

Assume we have an interface `Ping` that we use to test connection to a server. It contains `execute` method which returns the ping time:

    :::java
    interface Ping {
        void setUri(String uri);
        int execute();
    }

There's a real implementation of `Ping` and it works well. Now we need to create a proxy class to be able to wrap an implementation and substitute some logic, e.g. divide the actual time of the `ping` by 2.


The main aspect of *Jeta Proxy* is that you need to create an abstract class and implement only the method you want to override:

    :::java
    public abstract class FixTheTruth implements Ping, AbstractProxy<Ping> {
        @Override
        public int execute() {
            return real().execute() / 2;
        }
    }

<span class="label label-info">Note</span> You can access the real `Ping` implementation via `real()` method.

Well, in our proxy-wrapper we invoke real implementation and divide its result by two. To create the proxy-class we need a host class in which we are going to use `@Proxy` feature:

    :::java
    public ProxyTest {
        @Proxy(FixTheTruth.class)
        Ping ping;

        public PingTest(Ping ping) {
            this.ping = ping;
        }

        public void test() {
            MetaHelper.createProxy(this, ping);
            System.out.println(String.format("ping '%s'...", ping.getUri()));
            System.out.println(String.format("done in %dms", ping.execute()));
        }
    }

###MetaHelper

Now, let's define the helper method:

    :::java
    public static void createProxy(Object master, Object real) {
        new ProxyController(getInstance().metasitory, master).createProxy(real);
    }

Please, follow [this link](/guide/meta-helper.html) if you have questions about *MetaHelper*.
