/*
 * Copyright 2016 Oleg Khalidov
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
 *
 */

package org.brooth.jeta.apt;

import com.google.common.base.Joiner;
import org.brooth.jeta.apt.metasitory.MetasitoryWriter;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * for debugging
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
class EchoMetasitoryWriter implements MetasitoryWriter {

    private ProcessingEnvironment env;
    protected Logger logger;

    public void open(ProcessingContext env) {
        this.env = env.processingEnv();
        this.logger = env.logger();
        logger.debug("open()");
    }

    public void write(MetacodeContext context) {
        String master = context.masterElement().toString();
        logger.debug(String.format("master: %1$s, metacode: %2$s, annotations: {%3$s}",
                master, MetacodeUtils.getMetacodeOf(env.getElementUtils(), master),
                Joiner.on(", ").join(context.metacodeAnnotations())));
    }

    public void close() {
        logger.debug("close()");
    }
}
