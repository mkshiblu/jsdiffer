package io.rminerx.core.api;

import io.jsrminer.uml.UMLAttribute;
import io.jsrminer.uml.UMLType;

import java.util.List;

public interface IClassDeclaration extends IDeclarationContainer {
    void addAttribute(UMLAttribute attribute);

    List<UMLAttribute> getAttributes();
}
