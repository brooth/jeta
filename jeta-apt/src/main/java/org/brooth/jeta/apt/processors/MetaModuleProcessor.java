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

package org.brooth.jeta.apt.processors;

import com.squareup.javapoet.*;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.inject.MetaEntityProvider;
import org.brooth.jeta.inject.MetaModule;
import org.brooth.jeta.inject.Module;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class MetaModuleProcessor extends AbstractProcessor {

    public MetaModuleProcessor() {
        super(Module.class);
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        TypeElement masterElement = (TypeElement) context.elements().iterator().next();
        Module annotation = masterElement.getAnnotation(Module.class);
        builder.addSuperinterface(ClassName.get(MetaModule.class));

        TypeName providerTypeName = ParameterizedTypeName.get(ClassName.get(MetaEntityProvider.class),
                WildcardTypeName.OBJECT);

        builder.addMethod(MethodSpec.methodBuilder("getProvider")
                .addAnnotation(Override.class)
                .addAnnotation(Nullable.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(providerTypeName)
                .addParameter(TypeName.get(Class.class), "scope")
                .addParameter(TypeName.get(Class.class), "entity")
                .addStatement("return null")
                .build());

        return false;
    }
}
