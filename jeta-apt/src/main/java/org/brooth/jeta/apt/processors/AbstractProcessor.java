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
import org.brooth.jeta.apt.ProcessingContext;
import org.brooth.jeta.apt.UtdProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class AbstractProcessor implements UtdProcessor {

    protected Class<? extends Annotation> annotation;
    
    protected ProcessingContext processingContext;

    public AbstractProcessor(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }
    
    public void init(ProcessingContext processingContext) {
    	this.processingContext = processingContext;
    }

    public Set<Class<? extends Annotation>> collectElementsAnnotatedWith() {
        return Collections.<Class<? extends Annotation>>singleton(annotation);
    }

    public Set<TypeElement> applicableMastersOfElement(Element element) {
        return Collections.singleton(MetacodeUtils.typeElementOf(element));
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean needReclaim() {
        return false;
    }

    public boolean ignoreUpToDate() {
        return false;
    }
}
