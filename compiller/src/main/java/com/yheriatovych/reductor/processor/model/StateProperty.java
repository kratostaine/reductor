package com.yheriatovych.reductor.processor.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.yheriatovych.reductor.Reducer;
import com.yheriatovych.reductor.processor.ValidationException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class StateProperty {
    public final String name;
    public final TypeMirror stateType;
    public final ExecutableElement executableElement;

    private StateProperty(String name, TypeMirror stateType, ExecutableElement executableElement) {
        this.name = name;
        this.stateType = stateType;
        this.executableElement = executableElement;
    }

    static StateProperty parseStateProperty(Element element) throws ValidationException {
        StateProperty stateProperty;
        if (element.getKind() != ElementKind.METHOD) return null;

        ExecutableElement executableElement = (ExecutableElement) element;
        String propertyName = executableElement.getSimpleName().toString();
        TypeMirror stateType = executableElement.getReturnType();

        if (!executableElement.getParameters().isEmpty())
            throw new ValidationException(executableElement, "state property accessor %s should not have any parameters", executableElement);

        if (stateType.getKind() == TypeKind.VOID) {
            throw new ValidationException(executableElement, "void is not allowed as return type for property method %s", executableElement);
        }

        stateProperty = new StateProperty(propertyName, stateType, executableElement);
        return stateProperty;
    }

    public TypeName getReducerInterfaceTypeName() {
        TypeName stateType = TypeName.get(this.stateType);
        if (stateType.isPrimitive()) {
            stateType = stateType.box();
        }
        return ParameterizedTypeName.get(ClassName.get(Reducer.class), stateType);
    }
}
