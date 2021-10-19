package io.jsrminer.uml.diff;

import io.rminerx.core.api.IClassDeclaration;

public class UMLClassRenameDiff extends UMLClassBaseDiff {

    public UMLClassRenameDiff(IClassDeclaration originalClass, IClassDeclaration renamedClass) {
        super(originalClass, renamedClass);
    }

    public IClassDeclaration getRenamedClass() {
        return nextClass;
    }

    public boolean isInSameFile() {
        return originalClass.getParentContainerQualifiedName().equals(nextClass.getParentContainerQualifiedName());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ");
        sb.append(originalClass.getQualifiedName());
        sb.append(" was renamed to ");
        sb.append(nextClass.getQualifiedName());
        sb.append("\n");
        return sb.toString();
    }
}
