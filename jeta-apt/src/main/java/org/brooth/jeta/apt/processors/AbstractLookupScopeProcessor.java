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
import javax.lang.model.element.AnnotationMirror;
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
            AnnotationMirror scopeConfigMirror = rootScopeElement == null ? null :
                MetacodeUtils.getAnnotation(rootScopeElement, ScopeConfig.class, elementUtils);
            if (scopeConfigMirror == null)
                throw new ProcessingException(rootScope + " is not a meta scope");

            String moduleClassStr = MetacodeUtils.getAnnotationValueAsString(scopeConfigMirror, "module");
            if (moduleClassStr != null) {
                TypeElement extModule = elementUtils.getTypeElement(moduleClassStr);
                if (extModule == null)
                    throw new ProcessingException("Can't find module " + moduleClassStr);
                return lookupEntityScope(extModule, rootScope, entityClassStr);
            }
        }

        // look up in ext scope?
        String extExtScopeStr = MetacodeUtils.getAnnotationValueAsString(
                elementUtils.getTypeElement(rootScope), Scope.class, "ext", elementUtils);
        if (extExtScopeStr != null)
            return lookupEntityScope(module, extExtScopeStr, entityClassStr);

        return null;
    }

    protected boolean isAssignableScope(String scope, String scope2) {
        if (scope.equals(scope2))
            return true;

        Elements elements = processingContext.processingEnv().getElementUtils();
        final TypeElement scopeElement = elements.getTypeElement(scope);
        AnnotationMirror scopeAnnotation = MetacodeUtils.getAnnotation(
                scopeElement, Scope.class, processingContext.processingEnv().getElementUtils());
        if (scopeAnnotation == null)
            throw new ProcessingException(scope + " is not a scope");

        String ext = MetacodeUtils.getAnnotationValueAsString(scopeAnnotation, "ext");
        return ext != null && (ext.equals(scope2) || isAssignableScope(ext, scope2));
    }

    protected boolean isVoid(String str) {
        return str.equals(Void.class.getCanonicalName());
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
}
