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

package org.brooth.jeta.apt.processors;

import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.Processor;
import org.brooth.jeta.apt.ProcessorEnvironment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class AbstractProcessor implements Processor {

    protected Class<? extends Annotation> annotation;

    public AbstractProcessor(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public Set<Class<? extends Annotation>> collectElementsAnnotatedWith() {
        return Collections.<Class<? extends Annotation>>singleton(annotation);
    }

    @Override
    public Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element) {
        return Collections.singleton(MetacodeUtils.typeElementOf(element));
    }

    @Override
    public boolean isEnabled(ProcessingEnvironment processingEnv) {
        String jetaDisable = processingEnv.getOptions().get("jetaDisable");
        if (jetaDisable != null) {
            for (String disable : jetaDisable.split(","))
                if (annotation.getSimpleName().matches(disable))
                    return false;
        }

        return true;
    }

    @Override
    public boolean needReclaim() {
        return false;
    }

    @Override
    public boolean ignoreMasterUpToDate(ProcessorEnvironment env) {
        return false;
    }
}
