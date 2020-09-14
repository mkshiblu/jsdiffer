package io.jsrminer.sourcetree;

import java.util.*;

public class SingleStatement extends Statement {
    // private List<AbstractExpression> expressionList;
    private Set<VariableDeclaration> variableDeclarations = new LinkedHashSet<>();
    private Set<String> variables = new LinkedHashSet<>();
    private Map<String, List<OperationInvocation>> methodInvocationMap = new LinkedHashMap<>();
    private Map<String, List<ObjectCreation>> creationMap = new LinkedHashMap<>();
    private Set<String> identifierArguments = new LinkedHashSet<>();

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

    public Map<String, List<ObjectCreation>> getCreationMap() {
        return creationMap;
    }

    /**
     * Returns arguments which are Invocations
     */
    public Set<String> getArgumentsWithIdentifiers() {
        return identifierArguments;
    }
}
