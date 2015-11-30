package org.javameta.apt.processors;

import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import org.javameta.apt.MetacodeContext;
import org.javameta.apt.MetacodeUtils;
import org.javameta.apt.ProcessorEnvironment;
import org.javameta.base.InjectMetacode;
import org.javameta.base.Meta;
import org.javameta.base.MetaEntityFactory;
import org.javameta.util.Factory;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class InjectProcessor extends SimpleProcessor {

    private final Map<TypeElement, String> factoryElements = new HashMap<>();

    private TypeName metaEntityFactoryTypeName = TypeName.get(MetaEntityFactory.class);

    public InjectProcessor() {
        super(Meta.class);
    }

    @Override
    public boolean process(ProcessorEnvironment env, TypeSpec.Builder builder) {
        MetacodeContext context = env.metacodeContext();
        ClassName masterClassName = ClassName.get(context.masterElement());
        builder.addSuperinterface(ParameterizedTypeName.get(
                ClassName.get(InjectMetacode.class), masterClassName));

        factoryElements.clear();
        ClassName metaFactoryClassName = ClassName.get(MetaEntityFactory.class);
        for (Boolean staticMeta = true; staticMeta != null; staticMeta = staticMeta ? false : null) {
            MethodSpec.Builder methodBuilder;
            if (staticMeta) {
                methodBuilder = MethodSpec.methodBuilder("applyStaticMeta")
                        .addParameter(metaFactoryClassName, "factory", Modifier.FINAL);

            } else {
                methodBuilder = MethodSpec.methodBuilder("applyMeta")
                        .addParameter(masterClassName, "master", Modifier.FINAL)
                        .addParameter(metaFactoryClassName, "factory", Modifier.FINAL);
            }

            methodBuilder
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class);

            for (Element element : env.elements()) {
                String elementTypeStr = element.asType().toString();
                TypeName elementTypeName = TypeName.get(element.asType());
                String fieldNameStr = element.getSimpleName().toString();

                String fieldStatement = null;
                if (staticMeta) {
                    if (element.getModifiers().contains(Modifier.STATIC))
                        fieldStatement = String.format("%1$s.%2$s = ", elementTypeStr, fieldNameStr);

                } else {
                    if (!element.getModifiers().contains(Modifier.STATIC))
                        fieldStatement = String.format("master.%1$s = ", fieldNameStr);
                }
                if (fieldStatement == null)
                    continue;

                String fieldTypeStr = env.processingEnv().getTypeUtils().erasure(element.asType()).toString();
                switch (fieldTypeStr) {
                    case "org.javameta.util.Provider": {
                        fieldTypeStr = getGenericType(elementTypeStr);
                        ClassName fieldClassName = ClassName.bestGuess(fieldTypeStr);

                        TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(elementTypeName)
                                .addMethod(MethodSpec.methodBuilder("get")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(fieldClassName)
                                        .addStatement("return ($T) factory.getMetaEntity($T.class).getInstance()",
                                                ClassName.bestGuess(MetacodeUtils.getMetacodeOf(
                                                        env.processingEnv().getElementUtils(), fieldTypeStr) + ".MetaEntity"),
                                                fieldClassName)
                                        .build())
                                .build();

                        methodBuilder.addStatement(fieldStatement + " $L", providerTypeSpec);
                        break;
                    }
                    case "org.javameta.util.Lazy": {
                        fieldTypeStr = getGenericType(elementTypeStr);
                        ClassName fieldClassName = ClassName.bestGuess(fieldTypeStr);

                        TypeSpec lazyTypeSpec = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(elementTypeName)
                                .addField(fieldClassName, "instance", Modifier.PRIVATE)
                                .addMethod(MethodSpec.methodBuilder("get")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(fieldClassName)
                                        .beginControlFlow("if(instance == null)")
                                        .beginControlFlow("synchronized (this)")
                                        .beginControlFlow("if(instance == null)")
                                        .addStatement("instance = ($T) factory.getMetaEntity($T.class).getInstance()",
                                                ClassName.bestGuess(MetacodeUtils.getMetacodeOf(
                                                        env.processingEnv().getElementUtils(), fieldTypeStr) + ".MetaEntity"),
                                                fieldClassName)
                                        .endControlFlow()
                                        .endControlFlow()
                                        .endControlFlow()
                                        .addStatement("return instance")
                                        .build())
                                .build();
                        methodBuilder.addStatement(fieldStatement + " $L", lazyTypeSpec);
                        break;
                    }
                    default:
                        methodBuilder.addStatement(fieldStatement + getResultStatement(env, elementTypeStr));
                        break;
                }
            }

            builder.addMethod(methodBuilder.build());
        }

        for (TypeElement element : factoryElements.keySet())
            buildFactoryImpl(builder, element, factoryElements.get(element), env.processingEnv().getElementUtils());

        return false;
    }

    private void buildFactoryImpl(TypeSpec.Builder builder, TypeElement element, String name, Elements elementsUtils) {
        TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addSuperinterface(ClassName.bestGuess(element.getQualifiedName().toString()))
                .addField(metaEntityFactoryTypeName, "factory", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(metaEntityFactoryTypeName, "factory")
                        .addStatement("this.factory = factory")
                        .build());

        for (Element subElement : element.getEnclosedElements())
            if (subElement.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) subElement;
                TypeMirror[] params = new TypeMirror[method.getParameters().size()];
                String[] paramValues = new String[params.length];
                int pi = 0;
                for (VariableElement param : method.getParameters()) {
                    params[pi] = param.asType();
                    paramValues[pi++] = param.getSimpleName().toString();
                }

                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(method.getReturnType()))
                        .addStatement("return (($T)\n\tfactory.getMetaEntity($T.class)).\n\t\tgetInstance($L)",
                                ClassName.bestGuess(MetacodeUtils.getMetacodeOf(elementsUtils,
                                        method.getReturnType().toString()) + ".MetaEntity"),
                                method.getReturnType(),
                                Joiner.on(", ").join(paramValues));

                for (int i = 0; i < params.length; i++)
                    methodSpec.addParameter(TypeName.get(params[i]), paramValues[i]);

                factoryBuilder.addMethod(methodSpec.build());

            }

        builder.addType(factoryBuilder.build());
    }

    private String getResultStatement(ProcessorEnvironment env, String elementTypeStr) {
        boolean classOf = elementTypeStr.startsWith("java.lang.Class");
        if (classOf)
            elementTypeStr = getGenericType(elementTypeStr);

        TypeElement typeElement = env.processingEnv().getElementUtils().getTypeElement(elementTypeStr);
        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            if (classOf)
                throw new IllegalStateException("Class of " + elementTypeStr + " not valid meta structure.");
            if (typeElement.getKind() != ElementKind.INTERFACE)
                throw new IllegalStateException(elementTypeStr + " is not an interface so not allowed to be used as a meta factory.");

            if (!factoryElements.containsKey(typeElement))
                factoryElements.put(typeElement, typeElement.getSimpleName().toString() + "Impl" + factoryElements.size());

            return String.format("new %s(factory)", factoryElements.get(typeElement));
        }

        return String.format("((%1$s.MetaEntity)\n\tfactory.getMetaEntity(%2$s.class))\n\t\t.%3$s",
                MetacodeUtils.getMetacodeOf(env.processingEnv().getElementUtils(), elementTypeStr),
                elementTypeStr,
                classOf ? "getEntityClass()" : "getInstance()");
    }

    private String getGenericType(String type) {
        return getGenericType(type, true);
    }

    private String getGenericType(String type, boolean unwrapClass) {
        String genericType;
        int index = type.indexOf('<');
        if (index == -1)
            throw new IllegalStateException(type + " not valid. Specify generic type.");

        genericType = type.substring(index + 1, type.lastIndexOf('>'));
        if (genericType.startsWith("? extends "))
            genericType = genericType.replace("? extends ", "");

        String toValidate = unwrapClass && genericType.startsWith("java.lang.Class") ?
                getGenericType(genericType, false) : genericType;
        if (!toValidate.matches("^[a-zA-Z0-9.]*"))
            throw new IllegalStateException(type + " not valid meta structure of generics.");

        return genericType;
    }


}
