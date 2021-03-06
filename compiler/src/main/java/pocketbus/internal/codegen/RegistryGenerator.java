package pocketbus.internal.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import pocketbus.SubscriptionRegistration;
import pocketbus.internal.PocketBusConst;
import pocketbus.internal.Registry;

public class RegistryGenerator {
    private final String packageName;
    private final TypeElement typeElement;
    private List<SubscriptionNode> subscriptionTrees;

    public RegistryGenerator(TypeElement typeElement, String packageName) {
        this.typeElement = typeElement;
        this.packageName = packageName;
    }

    public JavaFile generate() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(PocketBusConst.REGISTRY_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(Registry.class));

        generateMethod(classBuilder);

        return JavaFile.builder(packageName, classBuilder.build()).build();
    }

    private void generateMethod(TypeSpec.Builder classBuilder) {
        TypeVariableName t = TypeVariableName.get("T");
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(PocketBusConst.METHOD_GET_REGISTRAR)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(t)
                .addParameter(t, PocketBusConst.VAR_TARGET)
                .returns(SubscriptionRegistration.class);

        boolean first = true;
        for (SubscriptionNode node : subscriptionTrees) {
            first = writeRegistrars(methodBuilder, node, first);
        }
        if (!first) {
            methodBuilder.endControlFlow();
        }
        methodBuilder.addStatement("return null");

        classBuilder.addMethod(methodBuilder.build());
    }

    /**
     *
     *
     * @param methodBuilder
     * @param node
     * @param first first element in this tree
     * @return whether first was consumed
     */
    private boolean writeRegistrars(MethodSpec.Builder methodBuilder, SubscriptionNode node, boolean first) {
        boolean useIf = first;
        for (SubscriptionNode child : node.getChildren()) {
            useIf = writeRegistrars(methodBuilder, child, useIf);
        }
        TypeMirror targetType = node.getTypeMirror();
        if (useIf) {
            methodBuilder.beginControlFlow("if ($N instanceof $T)", PocketBusConst.VAR_TARGET, targetType);
        } else {
            methodBuilder.nextControlFlow("else if ($N instanceof $T)", PocketBusConst.VAR_TARGET, targetType);
        }
        ClassName registrarClass = ClassName.bestGuess(targetType + PocketBusConst.REGISTRATION_SUFFIX);
        methodBuilder.addStatement("return new $T(($T)$N)", registrarClass, targetType, PocketBusConst.VAR_TARGET);
        return false;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setSubscriptionTrees(List<SubscriptionNode> subscriptionTrees) {
        this.subscriptionTrees = subscriptionTrees;
    }
}
