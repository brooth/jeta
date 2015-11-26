package org.javameta.samples.proxy;

import org.javameta.proxy.AbstractProxy;
import org.javameta.proxy.Proxy;
import org.javameta.samples.MetaHelper;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ProxySample {

    public interface Ping {
        void setUri(String uri);
        String getUri();
        int execute();
    }

    public static class PingEmulator implements Ping {
        private String uri;

        @Override
        public void setUri(String uri) {
            this.uri = uri;
        }

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public int execute() {
            long ts = System.currentTimeMillis();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                //
            }
            return (int) (System.currentTimeMillis() - ts);
        }
    }

    public static abstract class FixTheTruth implements Ping, AbstractProxy<Ping> {
        @Override
        public int execute() {
            return real().execute() / 2;
        }
    }

    public static class PingTest {
        @Proxy(FixTheTruth.class)
        protected Ping ping;

        public PingTest(Ping ping) {
            this.ping = ping;
        }

        public void test() {
            MetaHelper.createProxy(this, ping);
            System.out.println(String.format("ping '%s'...", ping.getUri()));
            System.out.println(String.format("done in %dms", ping.execute()));
        }
    }

    public static void main(String[] args) {
        Ping googlePing = new PingEmulator();
        googlePing.setUri("http://google.com");
        new PingTest(googlePing).test();
    }
}

