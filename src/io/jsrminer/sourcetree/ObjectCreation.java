package io.jsrminer.sourcetree;

import java.util.List;

public class ObjectCreation extends Invocation {
    private String typeName;

    protected String expressionText;
    protected List<String> arguments;

    public ObjectCreation() {

    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getExpressionText() {
        return expressionText;
    }

    public void setExpressionText(String expressionText) {
        this.expressionText = expressionText;
    }

    public List<String> getArguments() {
        return arguments;
    }
}
