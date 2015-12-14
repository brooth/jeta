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

package org.brooth.jeta.samples.logger;

import org.brooth.jeta.log.Log;
import org.brooth.jeta.samples.MetaHelper;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class LoggerSample {
    @Log
    Logger logger;
    @Log("Jeta")
    Logger jetaLogger;

    public LoggerSample() {
        MetaHelper.createLogger(this, LoggerProvider.getInstance());
    }

    public void logAway() {
        logger.debug("Hello, Jeta!");
        jetaLogger.debug("Hi, You!");
    }

    public static void main(String[] args) {
        new LoggerSample().logAway();
    }
}
