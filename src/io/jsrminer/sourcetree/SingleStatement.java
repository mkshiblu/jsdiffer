package io.jsrminer.sourcetree;

import io.rminer.core.api.IAnonymousFunctionDeclaration;
import io.rminer.core.api.ICodeFragment;
import io.rminer.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SingleStatement extends Statement implements ICodeFragment {
    // private List<AbstractExpression> expressionList;
    private List<VariableDeclaration> variableDeclarations = new ArrayList<>();
    private List<String> variables = new ArrayList<>();
    private Map<String, List<OperationInvocation>> methodInvocationMap = new LinkedHashMap<>();
    private Map<String, List<ObjectCreation>> creationMap = new LinkedHashMap<>();
    private List<String> identifierArguments = new ArrayList<>();

    public List<String> stringLiterals = new ArrayList<>();
    public List<String> numberLiterals = new ArrayList<>();
    public List<String> nullLiterals = new ArrayList<>();
    public List<String> booleanLiterals = new ArrayList<>();
    private List<String> infixOperators = new ArrayList<>();
    private List<String> arrayAccesses = new ArrayList<>();
    private List<String> prefixExpressions = new ArrayList<>();
    private List<String> postfixExpressions = new ArrayList<>();
    protected List<TernaryOperatorExpression> ternaryOperatorExpressions = new ArrayList<>();
    //private List<IAnonymousClassDeclaration> anonymousClassDeclarations = new ArrayList<>();
    private List<IAnonymousFunctionDeclaration> anonymousFunctionDeclarations = new ArrayList<>();
    private List<IFunctionDeclaration> functionDeclarations = new ArrayList<>();

    public SingleStatement() {
    }

    @Override
    public int statementCount() {
        return 1;
    }

    /**
     * Returns the identifiers involved in this statement
     */
    @Override
    public List<String> getVariables() {
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

    @Override
    public List<String> getPostfixExpressions() {
        return this.postfixExpressions;
    }

    @Override
    public List<TernaryOperatorExpression> getTernaryOperatorExpressions() {
        return this.ternaryOperatorExpressions;
    }

    /**
     * Returns arguments which are Invocations
     */
    @Override
    public List<String> getIdentifierArguments() {
        return identifierArguments;
    }

    /**
     * Returns the variableName and declaration map of all the variables
     */
    @Override
    public List<VariableDeclaration> getVariableDeclarations() {
        return this.variableDeclarations;

    }

    //    @Override
//    public List<IAnonymousClassDeclaration> getAnonymousClassDeclarations() {
//        return this.anonymousClassDeclarations;
//    }
    @Override
    public List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations() {
        return this.anonymousFunctionDeclarations;
    }

    @Override
    public List<IFunctionDeclaration> getFunctionDeclarations() {
        return this.functionDeclarations;
    }

//    @Override
//    public VariableDeclaration getVariableDeclaration(String variableName) {
//        for (VariableDeclaration vd : this.getVariableDeclarations()) {
//            if (vd.variableName.equals(variableName))
//                return vd;
//        }
//        return null;
//    }

//    @Override
//    public VariableDeclaration findVariableDeclarationIncludingParent(String variableName) {
//        VariableDeclaration vd = this.getVariableDeclaration(variableName);
//        if (vd != null) {
//            return vd;
//        } else if (getParent() != null) {
//            // TODO PullUp    return parent.findVariableDeclarationIncludingParent(varibleName);
//        }
//        return null;
//    }
}
