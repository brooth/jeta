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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
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

                addReturnStatement(methodBuilder, env.processingEnv(), element.asType(),
                        Collections.<TypeMirror, String>emptyMap(), fieldStatement);
            }

            builder.addMethod(methodBuilder.build());
        }

        for (TypeElement element : factoryElements.keySet())
            buildFactoryImpl(builder, env.processingEnv(), element, factoryElements.get(element));

        return false;
    }

    private void buildFactoryImpl(TypeSpec.Builder builder, ProcessingEnvironment env, TypeElement element, String name) {
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
                Map<TypeMirror, String> params = new HashMap<>();
                for (VariableElement param : method.getParameters())
                    params.put(param.asType(), param.getSimpleName().toString());

                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(method.getReturnType()));

                for (TypeMirror type : params.keySet()) {
                    methodSpec.addParameter(TypeName.get(type), params.get(type), Modifier.FINAL);
                }

                addReturnStatement(methodSpec, env, method.getReturnType(), params, "return");
                factoryBuilder.addMethod(methodSpec.build());

            }

        builder.addType(factoryBuilder.build());
    }

    private void addReturnStatement(MethodSpec.Builder methodBuilder, ProcessingEnvironment env,
                                    TypeMirror returnTypeMirror, Map<TypeMirror, String> params,
                                    String statementPrefix) {

        String returnTypeStr = env.getTypeUtils().erasure(returnTypeMirror).toString();
        String getInstanceStr = String.format("getInstance(%s)", Joiner.on(", ").join(params.values()));

        switch (returnTypeStr) {
            case "org.javameta.util.Provider": {
                returnTypeStr = getGenericType(returnTypeMirror.toString());

                TypeSpec providerTypeSpec = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(TypeName.get(returnTypeMirror))
                        .addMethod(MethodSpec.methodBuilder("get")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(ClassName.bestGuess(returnTypeStr))
                                .addStatement("return " + getResultStatement(env, returnTypeStr, getInstanceStr))
                                .build())
                        .build();

                methodBuilder.addStatement(statementPrefix + " $L", providerTypeSpec);
                break;
            }
            case "org.javameta.util.Lazy": {
                returnTypeStr = getGenericType(returnTypeMirror.toString());
                ClassName returnClassName = ClassName.bestGuess(returnTypeStr);

                TypeSpec lazyTypeSpec = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(TypeName.get(returnTypeMirror))
                        .addField(returnClassName, "instance", Modifier.PRIVATE)
                        .addMethod(MethodSpec.methodBuilder("get")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(returnClassName)
                                .beginControlFlow("if(instance == null)")
                                .beginControlFlow("synchronized (this)")
                                .beginControlFlow("if(instance == null)")
                                .addStatement("instance = " + getResultStatement(env, returnTypeStr, getInstanceStr))
                                .endControlFlow()
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return instance")
                                .build())
                        .build();
                methodBuilder.addStatement(statementPrefix + " $L", lazyTypeSpec);
                break;
            }
            default:
                methodBuilder.addStatement(statementPrefix + getResultStatement(env, returnTypeMirror.toString(), getInstanceStr));
        }
    }

    private String getResultStatement(ProcessingEnvironment env, String elementTypeStr, String getInstanceStr) {
        boolean classOf = elementTypeStr.startsWith("java.lang.Class");
        if (classOf)
            elementTypeStr = getGenericType(elementTypeStr);

        TypeElement typeElement = env.getElementUtils().getTypeElement(elementTypeStr);
        Factory factory = typeElement.getAnnotation(Factory.class);
        if (factory != null) {
            if (classOf)
                throw new IllegalStateException("Class of " + elementTypeStr + " not valid meta structure.");
            if (typeElement.getKind() != ElementKind.INTERFACE)
                throw new IllegalStateException(elementTypeStr + " only interfaces allowed to be used as a meta factory.");

            if (!factoryElements.containsKey(typeElement))
                factoryElements.put(typeElement, typeElement.getSimpleName().toString() + "Impl" + factoryElements.size());

            return String.format("new %s(factory)", factoryElements.get(typeElement));
        }

        return String.format("((%1$s.MetaEntity)\n\tfactory.getMetaEntity(%2$s.class))\n\t\t\t.%3$s",
                MetacodeUtils.getMetacodeOf(env.getElementUtils(), elementTypeStr),
                elementTypeStr,
                classOf ? "getEntityClass()" : getInstanceStr);
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
        if (!toValidate.matches("^[a-zA-Z0-9._$]*"))
            throw new IllegalStateException(type + " not valid meta structure of generics.");

        return genericType;
    }


}
