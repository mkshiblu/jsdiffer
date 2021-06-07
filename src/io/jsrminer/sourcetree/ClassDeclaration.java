package io.jsrminer.sourcetree;

import io.jsrminer.uml.UMLAttribute;
import io.jsrminer.uml.UMLType;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.entities.DeclarationContainer;

import java.util.LinkedList;
import java.util.List;

public class ClassDeclaration extends DeclarationContainer implements IClassDeclaration {

    private UMLType superClass;

    protected List<UMLAttribute> attributes = new LinkedList<>();

    @Override
    public String getFullyQualifiedName() {
        return null;
    }

    public UMLType getSuperClass() {
        return superClass;
    }

    public void setSuperClass(UMLType superClass) {
        this.superClass = superClass;
    }

    public void addAttribute(UMLAttribute attribute) {
        this.attributes.add(attribute);
    }
}
