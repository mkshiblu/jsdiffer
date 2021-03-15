package io.jsrminer.sourcetree;

import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.entities.DeclarationContainer;

public class ClassDeclaration extends DeclarationContainer implements IClassDeclaration {

    private ClassDeclaration superClass;

    @Override
    public String getFullyQualifiedName() {
        return null;
    }

    public ClassDeclaration getSuperClass() {
        return superClass;
    }

    public void setSuperClass(ClassDeclaration superClass) {
        this.superClass = superClass;
    }
}
