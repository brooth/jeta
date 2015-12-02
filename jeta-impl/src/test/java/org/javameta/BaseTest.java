package org.javameta;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class BaseTest {

    protected BaseTest() {
        TestMetaHelper.createLogger(this);
    }

    protected boolean sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
            return true;

        } catch (InterruptedException e) {
            return false;
        }
    }
}
