/*
 * Copyright 2015 Oleg Khalidov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.brooth.jeta.tests.log;

import org.brooth.jeta.BaseTest;
import org.brooth.jeta.Logger;
import org.brooth.jeta.MetaHelper;
import org.brooth.jeta.log.Log;
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
            MetaHelper.createLogger(this);
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
