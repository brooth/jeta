/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brooth.jeta.samples.proxy;

import org.brooth.jeta.proxy.Proxy;
import org.brooth.jeta.samples.MetaHelper;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ProxySample {

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

