package io.jsrminer.uml;

import javax.xml.namespace.QName;

public class UMLParameter {
    public final String name;
    private String defaultValue;    // IT  could be expression, function call or can use an earlier parameter
    private int indexPositionInParent; // Move to FD?

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
        if (this.defaultValue == parameter.defaultValue)
            return true;
        return this.defaultValue != null && this.defaultValue.equals(parameter.defaultValue))
    }

    public int getIndexPositionInParent() {
        return indexPositionInParent;
    }

    public void setIndexPositionInParent(int indexPositionInParent) {
        this.indexPositionInParent = indexPositionInParent;
    }
}
