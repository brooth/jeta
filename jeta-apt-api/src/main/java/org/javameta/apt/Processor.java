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

package org.javameta.apt;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Processor {

    /*
     * @return true if next round is needed
     */
    boolean process(ProcessorEnvironment env, TypeSpec.Builder builder);

    /*
     * Tell to MetacodeProcessor the annotations, it should collect elements with.
     * All the elements will passed to this processor in generating metacode stage.
     */
    Set<Class<? extends Annotation>> collectElementsAnnotatedWith();

    /*
     * Ensure type elements (masters elements) associated with @param element
     * For those elements metacode will be generated.
     */
    Set<TypeElement> applicableMastersOfElement(ProcessingEnvironment env, Element element);

    /*
     * No mater if master's source code hasn't been changed since its meta code generated,
     * return true to rebuild it
     */
    boolean forceOverwriteMetacode();
    
	/**
	 * return true if current rounds set of annotations is needed in the next round
	 */
	boolean needReclaim();
}
