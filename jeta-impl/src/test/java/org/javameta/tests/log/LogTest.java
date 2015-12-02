package org.javameta.tests.log;

import org.javameta.BaseTest;
import org.javameta.Logger;
import org.javameta.TestMetaHelper;
import org.javameta.log.Log;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class LogTest extends BaseTest {

    @Log
    Logger logger;
    @Log("Named")
    Logger customNameLogger;

    public static class LoggerHolder {
        @Log
        Logger logger;
        @Log("Inner")
        Logger customNameLogger;

        private LoggerHolder() {
            TestMetaHelper.createLogger(this);
        }
    }

    @Test
    public void testClassName() {
        LoggerHolder loggerHolder = new LoggerHolder();
        assertNotNull(logger);
        assertNotNull(loggerHolder.logger);

        logger.debug("testClassName()");

        assertEquals(logger.getName(), LogTest.class.getSimpleName());
        assertEquals(loggerHolder.logger.getName(), LoggerHolder.class.getSimpleName());
    }

    @Test
    public void testCustomName() {
        LoggerHolder loggerHolder = new LoggerHolder();
        assertNotNull(customNameLogger);
        assertNotNull(loggerHolder.customNameLogger);

        logger.debug("testCustomName()");

        assertEquals(customNameLogger.getName(), "Named");
        assertEquals(loggerHolder.customNameLogger.getName(), "Inner");
    }
}
