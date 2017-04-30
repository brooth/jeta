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

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public interface Processor {

	void init(ProcessingContext processingContext);
	
    /**
     * return false to disable processor
     */
    boolean isEnabled();

    /**
     * @return true if next round is needed
     */
    boolean process(TypeSpec.Builder builder, RoundContext context);

    /**
     * Tell to MetacodeProcessor the annotations, it should collect elements with.
     * All the elements will passed to this processor in generating metacode stage.
     */
    Set<TypeElement> collectElementsAnnotatedWith();

    /**
     * Ensure type elements (masters elements) associated with @param element
     * For those elements metacode will be generated.
     */
    Set<TypeElement> applicableMastersOfElement(Element element);

	/**
	 * return true if current rounds set of annotations is needed in the next round
	 */
	boolean needReclaim();

    /**
     * If masters source code hasn't been changed since its meta code generated,
     * return false to avoid regenerating
     */
    boolean ignoreUpToDate();
}
