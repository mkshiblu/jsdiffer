package io.jsrminer.sourcetree;

import java.util.*;

public class SingleStatement extends Statement {
    // private List<AbstractExpression> expressionList;
    private Set<VariableDeclaration> variableDeclarations = new LinkedHashSet<>();
    private Set<String> variables = new LinkedHashSet<>();
    private Map<String, List<OperationInvocation>> methodInvocationMap = new LinkedHashMap<>();

    public SingleStatement() {
    }


    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns the identifiers involved in this statement
     */
    public Set<String> getVariables() {
        return this.variables;
    }

    public void setVariables(Set<String> variables) {
        this.variables = variables;
    }

    public Set<VariableDeclaration> getVariableDeclarations() {
        return variableDeclarations;
    }

    public Map<String, List<OperationInvocation>> getMethodInvocationMap() {
        return methodInvocationMap;
    }
}
