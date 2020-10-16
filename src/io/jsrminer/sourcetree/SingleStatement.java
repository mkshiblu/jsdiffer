package io.jsrminer.sourcetree;

import java.util.*;

public class SingleStatement extends Statement {
    // private List<AbstractExpression> expressionList;
    private List<VariableDeclaration> variableDeclarations = new ArrayList<>();
    private Set<String> variables = new LinkedHashSet<>();
    private Map<String, List<OperationInvocation>> methodInvocationMap = new LinkedHashMap<>();
    private Map<String, List<ObjectCreation>> creationMap = new LinkedHashMap<>();
    private Set<String> identifierArguments = new LinkedHashSet<>();

    public List<String> stringLiterals = new ArrayList<>();
    public List<String> numberLiterals = new ArrayList<>();
    public List<String> nullLiterals = new ArrayList<>();
    public List<String> booleanLiterals = new ArrayList<>();
    private List<String> infixOperators = new ArrayList<>();
    private List<String> arrayAccesses = new ArrayList<>();
    private List<String> prefixExpressions = new ArrayList<>();

    public SingleStatement() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns the identifiers involved in this statement
     */
    @Override
    public Set<String> getVariables() {
        return this.variables;
    }

    @Override
    public Map<String, List<OperationInvocation>> getMethodInvocationMap() {
        return methodInvocationMap;
    }

    @Override
    public Map<String, List<ObjectCreation>> getCreationMap() {
        return creationMap;
    }

    @Override
    public List<String> getStringLiterals() {
        return this.stringLiterals;
    }

    @Override
    public List<String> getNumberLiterals() {
        return this.numberLiterals;
    }

    @Override
    public List<String> getNullLiterals() {
        return this.nullLiterals;
    }

    @Override
    public List<String> getBooleanLiterals() {
        return this.booleanLiterals;
    }

    @Override
    public List<String> getInfixOperators() {
        return infixOperators;
    }

    @Override
    public List<String> getArrayAccesses() {
        return arrayAccesses;
    }

    @Override
    public List<String> getPrefixExpressions() {
        return prefixExpressions;
    }

    /**
     * Returns arguments which are Invocations
     */
    @Override
    public Set<String> getIdentifierArguments() {
        return identifierArguments;
    }

    /**
     * Returns the variableName and declaration map of all the variables
     */
    @Override
    public List<VariableDeclaration> getVariableDeclarations() {
        return this.variableDeclarations;
    }

    @Override
    public VariableDeclaration getVariableDeclaration(String variableName) {
        for (VariableDeclaration vd : this.getVariableDeclarations()) {
            if (vd.variableName.equals(variableName))
                return vd;
        }
        return null;
    }

    @Override
    public VariableDeclaration findVariableDeclarationIncludingParent(String varibleName) {
        VariableDeclaration vd = this.getVariableDeclaration(varibleName);
        if (vd != null) {
            return vd;
        } else if (getParent() != null) {
            // TODO PullUp    return parent.findVariableDeclarationIncludingParent(varibleName);
        }
        return null;
    }
}
