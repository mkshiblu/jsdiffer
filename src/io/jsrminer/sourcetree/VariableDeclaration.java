package io.jsrminer.sourcetree;

public class VariableDeclaration extends CodeEntity {
    private Expression initializer;
    private VariableDeclarationKind kind;
    public final String variableName;
    private SourceLocation variableScope;
    private boolean isParameter;

    public VariableDeclaration(String variableName) {
        this.variableName = variableName;
    }

    public VariableDeclaration(String variableName, Expression optionalInitializer) {
        this(variableName);
        this.initializer = optionalInitializer;
    }

    /**
     * Returns the initializer of this variable declaration, or
     * <code>null</code> if there is none.
     *
     * @return the initializer expression node, or <code>null</code> if
     * there is none
     */
    public Expression getInitializer() {
        return this.initializer;
    }

    public void setInitializer(Expression initializer) {
        this.initializer = initializer;
    }

    public void setKind(VariableDeclarationKind kind) {
        this.kind = kind;
    }

    public VariableDeclarationKind getKind() {
        return kind;
    }

    public String getVariableName() {
        return variableName;
    }

    public SourceLocation getScope() {
        return variableScope;
    }

    public void setVariableScope(SourceLocation variableScope) {
        this.variableScope = variableScope;
    }

    public boolean isParameter() {
        return isParameter;
    }

    public void setIsParameter(boolean isParameter) {
        this.isParameter = isParameter;
    }
}
