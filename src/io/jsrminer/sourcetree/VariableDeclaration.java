package io.jsrminer.sourcetree;

import org.eclipse.jgit.annotations.Nullable;

public class VariableDeclaration extends CodeEntity {

    @Nullable Expression initializer;
    private final VariableDeclarationKind kind;
    public final String variableName;
    private SourceLocation scope;
    private boolean isParameter;

    public VariableDeclaration(String variableName, VariableDeclarationKind kind) {
        this.variableName = variableName;
        this.kind = kind;
        this.setType(CodeElementType.VARIABLE_DECLARATION);
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
        return scope;
    }

    public void setScope(SourceLocation scope) {
        this.scope = scope;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableDeclaration other = (VariableDeclaration) obj;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        if (variableName == null) {
            if (other.variableName != null)
                return false;
        } else if (!variableName.equals(other.variableName))
            return false;
        return true;
    }
}
