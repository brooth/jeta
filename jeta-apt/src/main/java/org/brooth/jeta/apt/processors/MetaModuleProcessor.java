package org.brooth.jeta.apt.processors;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingContext;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.inject.MetaEntity;
import org.brooth.jeta.inject.Module;
import org.brooth.jeta.inject.ModuleConfig;
import org.brooth.jeta.inject.Scope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author khalidov
 * @version $Id$
 */

public class MetaModuleProcessor extends AbstractProcessor {

    private Set<? extends Element> allMetaEntities;

    @Nullable
    private String defaultScopeStr;

    public MetaModuleProcessor() {
        super(Module.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        defaultScopeStr = processingContext.processingProperties().getProperty("meta.scope.default", null);
    }

    @Override
    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        if (allMetaEntities != null)
            throw new ProcessingException("Multiple modules defined");

        TypeElement moduleElement = (TypeElement) context.elements().iterator().next();
        final Module annotation = moduleElement.getAnnotation(Module.class);
        List<String> scopesStr = getScopesClasses(annotation);

        List<TypeElement> scopes = Lists.transform(scopesStr, new Function<String, TypeElement>() {
            @Override
            public TypeElement apply(String input) {
                TypeElement scopeElement = processingContext.processingEnv().getElementUtils().getTypeElement(input);
                Scope scopeAnnotation = scopeElement.getAnnotation(Scope.class);
                if (scopeAnnotation == null)
                    throw new ProcessingException(input + " is not a scope class. Put @Scope on it");
                return scopeElement;
            }
        });

        allMetaEntities = context.roundEnv().getElementsAnnotatedWith(MetaEntity.class);
        List<AnnotationSpec> scopeAnnotationBuilders = new ArrayList<>(scopes.size());
        AnnotationSpec.Builder moduleConfigAnnotationBuilder = AnnotationSpec.builder(ModuleConfig.class);

        for (TypeElement scopeElement : scopes) {
            Set<? extends Element> scopeEntities = getScopeEntities(scopeElement);
            Iterable<ClassName> entities = Iterables.transform(scopeEntities, new Function<Element, ClassName>() {
                @Override
                public ClassName apply(Element input) {
                    MetaEntity metaEntityAnnotation = input.getAnnotation(MetaEntity.class);
                    String ofTypeStr = getOfClass(metaEntityAnnotation);
                    if (isVoid(ofTypeStr))
                        return ClassName.get((TypeElement) input);
                    return ClassName.bestGuess(ofTypeStr);
                }
            });

            scopeAnnotationBuilders.add(AnnotationSpec.builder(ModuleConfig.ScopeConfig.class)
                    .addMember("\nscope", scopeElement.getQualifiedName().toString() + ".class")
                    .addMember("\nentities", !entities.iterator().hasNext() ? "{}" :
                                    "{\n" + Joiner.on(".class,\n").join(entities) + ".class\n}",
                            entities)
                    .build());
        }

        String extModuleStr = getExtClass(annotation);
        if (!isVoid(extModuleStr)) {
            moduleConfigAnnotationBuilder.addMember("ext", extModuleStr + ".class");
        }

        builder.addAnnotation(moduleConfigAnnotationBuilder
                .addMember("scopes", "{\n" + Joiner.on(",\n").join(scopeAnnotationBuilders) + "}",
                        scopeAnnotationBuilders)
                .build());

        return false;
    }

    @Nonnull
    private String getExtClass(final Module annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            @Override
            public void run() {
                annotation.ext();
            }
        });
    }

    @Nonnull
    private String getOfClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.of();
            }
        });
    }

    private List<String> getScopesClasses(final Module annotation) {
        return MetacodeUtils.extractClassesNames(new Runnable() {
            @Override
            public void run() {
                annotation.scopes();
            }
        });
    }

    private Set<? extends Element> getScopeEntities(TypeElement scopeElement) {
        final String scopeClassStr = scopeElement.getQualifiedName().toString();
        final boolean isDefaultScope = defaultScopeStr != null && defaultScopeStr.equals(scopeClassStr);

        return Sets.filter(allMetaEntities, new Predicate<Element>() {
            public boolean apply(Element input) {
                final MetaEntity a = input.getAnnotation(MetaEntity.class);
                String scope = MetacodeUtils.extractClassName(new Runnable() {
                    public void run() {
                        a.scope();
                    }
                });

                if (scopeClassStr.equals(scope))
                    return true;

                if (isVoid(scope)) {
                    if (defaultScopeStr == null)
                        throw new ProcessingException(input.getSimpleName().toString() + " has undefined scope. " +
                                "You need to set the scope to @MetaEntity(scope) or define default one as 'meta.scope.default' property");
                    if (isDefaultScope)
                        return true;
                }

                return false;
            }
        });
    }

    private boolean isVoid(String str) {
        return str.equals(Void.class.getCanonicalName());
    }
}
