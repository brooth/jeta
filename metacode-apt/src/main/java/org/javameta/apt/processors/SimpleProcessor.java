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

import org.javameta.apt.MetacodeUtils;
import org.javameta.apt.Processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class SimpleProcessor implements Processor {

    protected Class<? extends Annotation> annotation;

    public SimpleProcessor(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public void collectElementsAnnotatedWith(Set<Class<? extends Annotation>> set) {
        set.add(annotation);
    }

    @Override
    public Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element) {
        return Collections.singleton(MetacodeUtils.typeElementOf(element));
    }

    @Override
	public boolean needReclaim() {
		return false;
	}

    @Override
    public boolean forceOverwriteMetacode() {
        return false;
    }
}
