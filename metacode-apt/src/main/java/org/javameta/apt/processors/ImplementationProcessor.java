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

package org.javameta.apt.processors;

import com.squareup.javapoet.TypeSpec;
import org.javameta.apt.ProcessorContext;
import org.javameta.util.Implementation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class ImplementationProcessor extends SimpleProcessor {

    public ImplementationProcessor() {
        super(Implementation.class);
    }

    @Override
    public boolean process(ProcessingEnvironment env, RoundEnvironment roundEnv, ProcessorContext ctx, TypeSpec.Builder builder, int round) {
        return false;
    }
}
