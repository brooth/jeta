package com.github.brooth.metacode.samples.proxy;

import com.github.brooth.metacode.proxy.AbstractProxy;
import com.github.brooth.metacode.proxy.Proxy;
import com.github.brooth.metacode.samples.MetaHelper;

/**
 *
 */
public class ProxySample {

    public interface Ping {
        void setUri(String uri);
        String getUti();
        int execute();
    }

    public static class PingImpl implements Ping {
        private String uri;

        @Override
        public void setUri(String uri) {
            this.uri = uri;
        }

        @Override
        public String getUti() {
            return uri;
        }

        @Override
        public int execute() {
            long ts = System.currentTimeMillis();
            //...
            return (int) (System.currentTimeMillis() - ts);
        }
    }

    public static abstract class FakePing implements Ping, AbstractProxy<Ping> {
        @Override
        public int execute() {
            return 42;
        }
    }

    public static class PingTest {
        @Proxy(FakePing.class)
        protected Ping ping;

        public PingTest(Ping ping) {
            this.ping = ping;
        }

        public void test() {
            MetaHelper.createProxy(this, ping);
            System.out.println(String.format("'%s' pinged in %dms", ping.getUti(), ping.execute()));
        }
    }

    public static void main(String[] args) {
        Ping googlePing = new PingImpl();
        googlePing.setUri("http://google.com");
        new PingTest(googlePing).test();
    }
}

