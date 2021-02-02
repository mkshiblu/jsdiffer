package io.jsrminer.sourcetree;

import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Expression extends CodeFragment implements ILeafFragment {
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
    private BlockStatement ownerBlock;

    @Override
    public BlockStatement getParent() {
        return ownerBlock;
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

    @Override
    public VariableDeclaration getVariableDeclaration(String variableName) {
        for (VariableDeclaration vd : this.getVariableDeclarations()) {
            if (vd.variableName.equals(variableName))
                return vd;
        }
        return null;
    }

    @Override
    public VariableDeclaration findVariableDeclarationIncludingParent(String variableName) {
        VariableDeclaration variableDeclaration = this.getVariableDeclaration(variableName);
        if (variableDeclaration != null) {
            return variableDeclaration;
        } else if (ownerBlock != null) {
            return ownerBlock.findVariableDeclarationIncludingParent(variableName);
        }
        return null;
    }
//    @Override
//    public List<IAnonymousClassDeclaration> getAnonymousClassDeclarations() {
//        return anonymousClassDeclarations;
//    }

    @Override
    public List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations() {
        return anonymousFunctionDeclarations;
    }

    @Override
    public List<IFunctionDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }

    /**
     * Returns the block statement this expression belongs to
     *
     * @return
     */
    public BlockStatement getOwnerBlock() {
        return ownerBlock;
    }

    public void setOwnerBlock(BlockStatement ownerBlock) {
        this.ownerBlock = ownerBlock;
    }

    public List<String> getPostfixExpressions() {
        return postfixExpressions;
    }

    @Override
    public List<TernaryOperatorExpression> getTernaryOperatorExpressions() {
        return this.ternaryOperatorExpressions;
    }
}
