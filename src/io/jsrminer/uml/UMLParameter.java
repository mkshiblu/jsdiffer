package io.jsrminer.uml;

import io.jsrminer.sourcetree.CodeEntity;
import io.jsrminer.sourcetree.VariableDeclaration;

public class UMLParameter extends CodeEntity {
    public final String name;
    private String defaultValue;    // IT  could be expression, function call or can use an earlier parameter
    private int indexPositionInParent; // Move to FD?
    private VariableDeclaration variableDeclaration;

    public UMLParameter(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasSameDefaultValue(UMLParameter parameter) {
        if (this.defaultValue == null && parameter.defaultValue == null)
            return true;
        return this.defaultValue.equals(parameter.defaultValue);
    }

    public int getIndexPositionInParent() {
        return indexPositionInParent;
    }

    public void setIndexPositionInParent(int indexPositionInParent) {
        this.indexPositionInParent = indexPositionInParent;
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    public VariableDeclaration getVariableDeclaration() {
        return variableDeclaration;
    }

    public void setVariableDeclaration(VariableDeclaration variableDeclaration) {
        this.variableDeclaration = variableDeclaration;
    }

    @Override
    public String toString() {
        return name;
    }
}
