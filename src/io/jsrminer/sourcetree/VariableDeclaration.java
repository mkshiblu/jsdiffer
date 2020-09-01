package io.jsrminer.sourcetree;

public class VariableDeclaration {

    private Expression initializer;
    public final String variableName;

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
}