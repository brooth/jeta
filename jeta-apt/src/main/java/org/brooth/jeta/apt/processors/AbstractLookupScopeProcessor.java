package org.brooth.jeta.apt.processors;

import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.inject.ModuleConfig;
import org.brooth.jeta.inject.Scope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author khalidov
 * @version $Id$
 */
public abstract class AbstractLookupScopeProcessor extends AbstractProcessor {

    // todo: speedup. with respect rootScope
//    private Map<String, String> entitiesScopesCache;

    public AbstractLookupScopeProcessor(Class<? extends Annotation> annotation) {
        super(annotation);
    }

    @Nullable
    protected String lookupEntityScope(TypeElement module, String rootScope, String entityClassStr) {
        if (isVoid(rootScope))
            return null;

//        if (entitiesScopesCache == null)
//            entitiesScopesCache = new HashMap<>();
//        if (entitiesScopesCache.containsKey(entityClassStr))
//            return entitiesScopesCache.get(entityClassStr);

        Elements elementUtils = processingContext.processingEnv().getElementUtils();
        String moduleName = module.getQualifiedName().toString();
        TypeElement metaModuleElement = elementUtils.getTypeElement(MetacodeUtils.toMetacodeName(moduleName));
        ModuleConfig moduleConfig = metaModuleElement != null ? metaModuleElement.getAnnotation(ModuleConfig.class) : null;
        if (moduleConfig == null)
            throw new ProcessingException("Unknown error. Meta module config for '" + moduleName + "' not found or broken");

        for (final ModuleConfig.ScopeConfig scopeConfig : moduleConfig.scopes()) {
            String scopeClassStr = getScopeClass(scopeConfig);
            if (rootScope.equals(scopeClassStr)) {
                List<String> scopeElements = getEntitiesClasses(scopeConfig);
//                for (String scopeElement : scopeElements)
//                    entitiesScopesCache.put(scopeElement, scopeClassStr);
                if (scopeElements.contains(entityClassStr))
                    return scopeClassStr;
                break;
            }
        }

        // no in current module, current ext scope. in ext-ext scope?
        Scope extExtScopeAnnotation = elementUtils.getTypeElement(rootScope).getAnnotation(Scope.class);
        String extExtScopeStr = getExtClass(extExtScopeAnnotation);
        if (!isVoid(extExtScopeStr))
            extExtScopeStr = lookupEntityScope(module, extExtScopeStr, entityClassStr);
        else
            extExtScopeStr = null;

        // in ext module?
        if (extExtScopeStr == null) {
            String extModuleClassStr = getExtClass(moduleConfig);
            if (!isVoid(extModuleClassStr)) {
                TypeElement extModuleTypeElement = elementUtils.getTypeElement(extModuleClassStr);
                if (extModuleTypeElement == null)
                    throw new ProcessingException(extModuleClassStr + " is not valid module. Can't extend");

                return lookupEntityScope(extModuleTypeElement, rootScope, entityClassStr);
            }
        }

        return extExtScopeStr;
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
    protected String getExtClass(final ModuleConfig annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            @Override
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
    protected String getScopeClass(final ModuleConfig.ScopeConfig scopeConfig) {
        return MetacodeUtils.extractClassName(new Runnable() {
            @Override
            public void run() {
                scopeConfig.scope();
            }
        });
    }
}
