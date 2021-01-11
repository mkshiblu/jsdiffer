package io.jsrminer.sourcetree;

public class VariableDeclaration extends CodeEntity {
    private Expression initializer;
    private final VariableDeclarationKind kind;
    public final String variableName;
    private SourceLocation variableScope;
    private boolean isParameter;

    public VariableDeclaration(String variableName, VariableDeclarationKind kind) {
        this.variableName = variableName;
        this.kind = kind;
    }

    public VariableDeclaration(String variableName, Expression optionalInitializer, VariableDeclarationKind kind) {
        this(variableName, kind);
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

    public boolean isGlobal() {
        return this.kind.equals(VariableDeclarationKind.GLOBAL);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(variableName);//.append(" : ").append(kind.name());
//        if(varargsParameter) {
//            sb.append("...");
//        }
        return sb.toString();
    }
}
