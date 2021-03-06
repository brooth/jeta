/*
 * Copyright 2016 Oleg Khalidov
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

package org.brooth.jeta;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class Logger {

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void debug(String msg) {
        synchronized (System.out) {
            System.out.println(String.format("debug [%s] %s", name, msg));
        }
    }

    public void debug(String msg, Object... params) {
        debug(String.format(msg, params));
    }

    public void error(String msg) {
        synchronized (System.err) {
            System.err.println(String.format("error [%s] %s", name, msg));
        }
    }

    public void error(String msg, Object... params) {
        error(String.format(msg, params));
    }
}
