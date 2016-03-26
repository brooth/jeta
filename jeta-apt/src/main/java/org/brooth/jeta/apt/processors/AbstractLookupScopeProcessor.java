/*
 *  Copyright 2016 Oleg Khalidov
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
 *
 */

package org.brooth.jeta.apt.processors;

import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.inject.ModuleConfig;
import org.brooth.jeta.inject.Scope;
import org.brooth.jeta.inject.ScopeConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public abstract class AbstractLookupScopeProcessor extends AbstractProcessor {

    public AbstractLookupScopeProcessor(Class<? extends Annotation> annotation) {
        super(annotation);
    }

    @Nullable
    protected String lookupEntityScope(TypeElement module, String rootScope, String entityClassStr) {
        if (isVoid(rootScope))
            return null;

        Elements elementUtils = processingContext.processingEnv().getElementUtils();
        String moduleName = module.getQualifiedName().toString();
        TypeElement metaModuleElement = elementUtils.getTypeElement(MetacodeUtils.toMetacodeName(moduleName));
        ModuleConfig moduleConfig = metaModuleElement != null ? metaModuleElement.getAnnotation(ModuleConfig.class) : null;
        if (moduleConfig == null)
            throw new ProcessingException("Unknown error. Meta module config for '" + moduleName + "' not found or broken");

        boolean existsInModule = false;
        for (final ModuleConfig.ScopeConfig scopeConfig : moduleConfig.scopes()) {
            String scopeClassStr = getScopeClass(scopeConfig);
            if (rootScope.equals(scopeClassStr)) {
                String moduleClassStr = getModuleClass(scopeConfig);
                if (isVoid(moduleClassStr)) {
                    List<String> scopeElements = getEntitiesClasses(scopeConfig);
                    if (scopeElements.contains(entityClassStr))
                        return scopeClassStr;

                } else {
                    TypeElement extModuleElement = elementUtils.getTypeElement(moduleClassStr);
                    return lookupEntityScope(extModuleElement, rootScope, entityClassStr);
                }

                existsInModule = true;
                break;
            }
        }

        // look up in ext module?
        if (!existsInModule) {
            TypeElement rootScopeElement = elementUtils.getTypeElement(MetacodeUtils.toMetacodeName(rootScope));
            ScopeConfig scopeConfigAnnotation = rootScopeElement != null ? rootScopeElement.getAnnotation(ScopeConfig.class) : null;
            if (scopeConfigAnnotation == null)
                throw new ProcessingException(rootScope + " is not a meta scope");

            String moduleClassStr = getModuleClass(scopeConfigAnnotation);
            TypeElement extModule = elementUtils.getTypeElement(moduleClassStr);
            if (extModule == null)
                throw new ProcessingException("Can't find module " + moduleClassStr);
            return lookupEntityScope(extModule, rootScope, entityClassStr);
        }

        // look up in ext scope?
        Scope extExtScopeAnnotation = elementUtils.getTypeElement(rootScope).getAnnotation(Scope.class);
        String extExtScopeStr = getExtClass(extExtScopeAnnotation);
        if (!isVoid(extExtScopeStr))
            return lookupEntityScope(module, extExtScopeStr, entityClassStr);

        return null;
    }

    protected boolean isVoid(String str) {
        return str.equals(Void.class.getCanonicalName());
    }

    @Nonnull
    protected String getExtClass(final Scope annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.ext();
            }
        });
    }

    @Nonnull
    protected List<String> getEntitiesClasses(final ModuleConfig.ScopeConfig scopeConfig) {
        return MetacodeUtils.extractClassesNames(new Runnable() {
            public void run() {
                scopeConfig.entities();
            }
        });
    }

    @Nonnull
    protected String getModuleClass(final ModuleConfig.ScopeConfig scopeConfig) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                scopeConfig.module();
            }
        });
    }

    @Nonnull
    protected String getScopeClass(final ModuleConfig.ScopeConfig scopeConfig) {
        return MetacodeUtils.extractClassName(new Runnable() {
            @Override
            public void run() {
                scopeConfig.scope();
            }
        });
    }

    @Nonnull
    protected String getModuleClass(final ScopeConfig scopeConfig) {
        return MetacodeUtils.extractClassName(new Runnable() {
            @Override
            public void run() {
                scopeConfig.module();
            }
        });
    }
}
