package org.brooth.jeta.apt.processors;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import org.brooth.jeta.Constructor;
import org.brooth.jeta.apt.MetacodeUtils;
import org.brooth.jeta.apt.ProcessingContext;
import org.brooth.jeta.apt.ProcessingException;
import org.brooth.jeta.apt.RoundContext;
import org.brooth.jeta.inject.IMetaEntity;
import org.brooth.jeta.inject.MetaEntity;
import org.brooth.jeta.inject.Scope;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author khalidov
 * @version $Id$
 */
public class MetaScopeProcessor extends AbstractProcessor {

    @Nullable
    private String defaultScopeStr;

    private static final String assignableStatement = "return $T.class == scope.getClass()";
    private static final String assignableExtStatement = assignableStatement + " || $T.isAssignable(scope)";

    private TypeElement scopeElement;
    private TypeMirror scopeTypeMirror;

    private Set<? extends Element> allMetaEntities;

    // MetaEntity.class -> Scope.class
    static Map<String, String> scopeEntities = null;

    public MetaScopeProcessor() {
        super(Scope.class);
    }

    @Override
    public void init(ProcessingContext processingContext) {
        super.init(processingContext);
        defaultScopeStr = processingContext.processingProperties().getProperty("meta.scope.default", null);

        if (scopeEntities == null)
            scopeEntities = new HashMap<String, String>();
    }

    public boolean process(TypeSpec.Builder builder, RoundContext context) {
        scopeElement = (TypeElement) context.elements().iterator().next();
        scopeTypeMirror = scopeElement.asType();

        final String scopeClassStr = context.metacodeContext().masterElement().getQualifiedName().toString();
        final boolean isDefaultScope = defaultScopeStr != null && defaultScopeStr.equals(scopeClassStr);

        if (allMetaEntities == null)
            allMetaEntities = context.roundEnv().getElementsAnnotatedWith(MetaEntity.class);

        Set<? extends Element> scopeEntities = Sets.filter(allMetaEntities, new Predicate<Element>() {
            public boolean apply(Element input) {
                final MetaEntity a = input.getAnnotation(MetaEntity.class);
                String scope = MetacodeUtils.extractClassName(new Runnable() {
                    public void run() {
                        a.scope();
                    }
                });

                if (scopeClassStr.equals(scope))
                    return true;

                if (scope.equals(Void.class.getCanonicalName())) {
                    if (defaultScopeStr == null)
                        throw new ProcessingException(input.getSimpleName().toString() + " has undefined scope. " +
                                "You need to set the scope to @MetaEntity(scope) or define default one as 'meta.scope.default' property");
                    if (isDefaultScope)
                        return true;
                }

                return false;
            }
        });

        if (scopeEntities.isEmpty()) {
            processingContext.logger().warn("Scope '" + scopeClassStr + "' has no entities.");
            return false;
        }

        // build packages tree
        PackageTree tree = new PackageTree();
        for (Element entityElement : scopeEntities) {
            MetaEntity annotation = entityElement.getAnnotation(MetaEntity.class);

            Element ofElement;
            String ofTypeStr = getOfClass(annotation);
            if (ofTypeStr.equals(Void.class.getCanonicalName()))
                ofElement = entityElement;
            else
                ofElement = processingContext.processingEnv().getElementUtils().getTypeElement(ofTypeStr);

            String pkg = processingContext.processingEnv().getElementUtils().getPackageOf(ofElement)
                    .getQualifiedName().toString();

            PackageTree.PackageNode parent = tree.root;
            for (String pkgNodeName : pkg.split("\\.")) {
                PackageTree.PackageNode node = parent.get(pkgNodeName);
                if (node == null) {
                    node = new PackageTree.PackageNode(parent, pkgNodeName);
                    parent.children.add(node);
                }
                parent = node;
            }
            parent.addElement((TypeElement) entityElement);
        }

        buildIsAssignableMethod(builder, context);

        for (PackageTree.PackageNode node : tree.root.children) {
            buildPackageNode(node, builder);
        }

        return false;
    }

    private void buildIsAssignableMethod(TypeSpec.Builder builder, RoundContext context) {
        MethodSpec.Builder assignableMethodBuilder = MethodSpec.methodBuilder("isAssignable")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(ClassName.OBJECT, "scope")
                .returns(boolean.class);

        String assignableStr = MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                scopeElement.getAnnotation(Scope.class).assignable();
            }
        });
        if (assignableStr.equals(Void.class.getCanonicalName())) {
            assignableMethodBuilder.addStatement(assignableStatement,
                    TypeName.get(context.metacodeContext().masterElement().asType()));
        } else {
            assignableMethodBuilder.addStatement(assignableExtStatement,
                    TypeName.get(context.metacodeContext().masterElement().asType()),
                    ClassName.bestGuess(MetacodeUtils.getMetacodeOf(processingContext.processingEnv().getElementUtils(),
                            assignableStr)));
        }

        builder.addMethod(assignableMethodBuilder.build());
    }

    private void buildPackageNode(PackageTree.PackageNode node, TypeSpec.Builder parentBuilder) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(node.name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        if (node.elements != null) {
            for (TypeElement element : node.elements) {
                String elementTypeStr = element.getQualifiedName().toString();
                final MetaEntity annotation = element.getAnnotation(MetaEntity.class);

                String ofTypeStr = getOfClass(annotation);
                if (ofTypeStr.equals(Void.class.getCanonicalName()))
                    ofTypeStr = elementTypeStr;
                String extTypeStr = getExtClass(annotation);
                if (extTypeStr.equals(Void.class.getCanonicalName()))
                    extTypeStr = null;

                ClassName ofClassName = ClassName.bestGuess(ofTypeStr);
                ClassName superClassName;
                if (extTypeStr == null) {
                    superClassName = ClassName.get(IMetaEntity.class);

                } else {
                    String pkg = processingContext.processingEnv().getElementUtils().getPackageOf(scopeElement)
                            .getQualifiedName().toString();
                    superClassName = ClassName.get(pkg, scopeElement.getSimpleName().toString() + "_Metacode." +
                            MetacodeUtils.getMetaNameOf(processingContext.processingEnv().getElementUtils(),
                                    extTypeStr, "_MetaEntity"));
                }

                boolean isSelfProvider = elementTypeStr.equals(ofTypeStr);

                String interfaceName = MetacodeUtils.getSimpleMetaNodeOf(
                        processingContext.processingEnv().getElementUtils(), ofTypeStr, "_MetaEntity");
                TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
                        .addJavadoc("emitted by " + elementTypeStr + '\n').addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(superClassName)
                        .addMethod(MethodSpec.methodBuilder("getEntityClass")
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ofClassName)))
                                .build());

                List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
                for (Element subElement : ((TypeElement) element).getEnclosedElements()) {
                    boolean validInitConstructor = element.getKind() == ElementKind.CLASS
                            && !subElement.getModifiers().contains(Modifier.PRIVATE)
                            && ((isSelfProvider && subElement.getSimpleName().contentEquals("<init>")) ||
                            subElement.getAnnotation(Constructor.class) != null);

                    if (validInitConstructor)
                        constructors.add((ExecutableElement) subElement);
                }

                for (ExecutableElement constructor : constructors) {
                    List<ParameterSpec> params = new ArrayList<ParameterSpec>();
                    params.add(ParameterSpec.builder(ClassName.OBJECT, "__scope__").build());
                    for (VariableElement input : constructor.getParameters()) {
                        TypeMirror paramType = input.asType();
                        if (!processingContext.processingEnv().getTypeUtils().isAssignable(paramType, scopeTypeMirror)) {
                            params.add(ParameterSpec.builder(TypeName.get(paramType), input.getSimpleName().toString()).build());
                        }
                    }

                    interfaceBuilder.addMethod(
                            MethodSpec.methodBuilder("getInstance")
                                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                    .returns(ofClassName).addParameters(params).build());
                }

                builder.addType(interfaceBuilder.build());
                scopeEntities.put(ofTypeStr, scopeElement.getQualifiedName().toString());
            }
        }
        for (PackageTree.PackageNode child : node.children)
            buildPackageNode(child, builder);

        parentBuilder.addType(builder.build());
    }

    private String getOfClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.of();
            }
        });
    }

    private String getExtClass(final MetaEntity annotation) {
        return MetacodeUtils.extractClassName(new Runnable() {
            public void run() {
                annotation.ext();
            }
        });
    }

    @Override
    public boolean ignoreUpToDate() {
        return true;
    }

    private static class PackageTree {
        private PackageNode root;

        private PackageTree() {
            root = new PackageNode(null, null);
        }

        private static class PackageNode {
            PackageNode parent;
            List<PackageNode> children;
            String name;
            List<TypeElement> elements;

            public PackageNode(PackageNode parent, String name) {
                this.parent = parent;
                this.children = new ArrayList<PackageNode>();
                this.name = name;
            }

            PackageNode get(final String name) {
                return Iterables.find(children, new Predicate<PackageNode>() {
                    public boolean apply(PackageNode input) {
                        return input.name.endsWith(name);
                    }
                }, null);
            }

            void addElement(TypeElement element) {
                if (elements == null)
                    elements = new ArrayList<TypeElement>();
                elements.add(element);
            }
        }
    }
}
