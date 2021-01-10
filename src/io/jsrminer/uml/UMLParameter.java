package io.jsrminer.uml;

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

    @Override
    public String toString() {
        return name;
    }
}
