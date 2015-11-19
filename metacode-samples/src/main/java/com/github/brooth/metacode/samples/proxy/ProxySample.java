package com.github.brooth.metacode.samples.proxy;

/**
 *
 */
public class ProxySample {

    public interface Ping {
        void setUri(String uri);
        int execute();
    }
        
    public static class PingImpl implements Ping {
        private String uri;
        
        public void setUri(String uri) {
            this.uri = uri;
        }

        public int execute() {                
            long ts = System.currentTimeMillis();
            //...
            return (int) System.currentTimeMillis() - ts;
        }
    }

    public abstract class FakePing implements Ping, AbstractProxy<Ping> {
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

        public void ping() {
            MetaHelper.createProxy(this, ping);
            System.out.println("ping result:  " + ping.execute());
        }
    }

    public static void main(String[] args) {
        Ping googlePing = new PingImpl();
        googlePing.setUri("http://google.com");
        PingTest test = new PingTest(googlePing);
        test.ping();
    }
}

public class PingTest_Metacode {
    public void applyProxy(PingTest master, final Object real) {
        if(real == master.ping) {
            master.ping = new FakePing() {
                protected Ping real() {
                    return real;
                }
    
                public setUri(String uri) {
                    real().setUri(uri);
                }
            }
        }
    }
}

