package io.jsrminer.sourcetree;

import io.rminer.core.api.IAnonymousFunctionDeclaration;
import io.rminer.core.api.ICompositeFragment;
import io.rminer.core.api.IFunctionDeclaration;

import java.util.*;

/**
 * A block statement, i.e., a sequence of statements surrounded by braces {}.
 * May contain other block statements or statements (i.e. composite statements)
 */
public class BlockStatement extends Statement implements ICompositeFragment {
    protected List<Statement> statements = new ArrayList<>();
    protected List<Expression> expressions = new ArrayList<>();
    //private List<VariableDeclaration> variableDeclarations;
    // exp
    // vd

    public BlockStatement() {
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    public void addExpression(Expression expression) {
        this.expressions.add(expression);
    }


    /**
     * Returns all the single statements including children's of children in a bottom up fashion
     * (i.e. lowest level children will be the first elements of the list sorted by their index position in parent)
     */
    public List<SingleStatement> getAllLeafStatementsIncludingNested() {
        final List<SingleStatement> leaves = new ArrayList<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                leaves.addAll(((BlockStatement) statement).getAllLeafStatementsIncludingNested());
            } else {
                leaves.add((SingleStatement) statement);
            }
        }
        return leaves;
    }

    public String getTextWithExpressions() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.text);
        if (expressions.size() > 0) {
            sb.append("(");
            for (int i = 0; i < expressions.size() - 1; i++) {
                sb.append(expressions.get(i).text).append("; ");
            }
            sb.append(expressions.get(expressions.size() - 1).toString());
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Similar to getInnerNodes of RM
     *
     * @return
     */
    public Set<BlockStatement> getAllBlockStatementsIncludingNested() {
        final Set<BlockStatement> innerNodes = new LinkedHashSet<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                innerNodes.addAll(composite.getAllBlockStatementsIncludingNested());
            }
        }
        innerNodes.add(this);
        return innerNodes;
    }

    public List<Statement> getStatements() {
        return this.statements;
    }

    public void setCodeElementType(CodeElementType type) {
        this.type = type;
    }

    /***
     * Returns the text with Expressions
     * @return
     */
    @Override
    public String getText() {
        return getTextWithExpressions();
    }

    @Override
    public List<String> getVariables() {
        List<String> variables = new ArrayList<>();
        for (Expression expression : this.getExpressions()) {
            variables.addAll(expression.getVariables());
        }
        return variables;
    }

    /**
     * Returns all variables inside including nested ones
     *
     * @return
     */
    public List<String> getAllVariablesIncludingNested() {
        List<String> variables = new ArrayList<>();
        variables.addAll(getVariables());
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                variables.addAll(composite.getAllVariablesIncludingNested());
            } else if (statement instanceof SingleStatement) {
                SingleStatement statementObject = (SingleStatement) statement;
                variables.addAll(statementObject.getVariables());
            }
        }
        return variables;
    }

    public List<IAnonymousFunctionDeclaration> getAllAnonymousFunctionDeclarations() {
        List<IAnonymousFunctionDeclaration> anonymousClassDeclarations = new ArrayList<>();
        anonymousClassDeclarations.addAll(getAnonymousFunctionDeclarations());
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                anonymousClassDeclarations.addAll(composite.getAllAnonymousFunctionDeclarations());
            } else if (statement instanceof SingleStatement) {
                SingleStatement statementObject = (SingleStatement) statement;
                anonymousClassDeclarations.addAll(statementObject.getAnonymousFunctionDeclarations());
            }
        }
        return anonymousClassDeclarations;
    }

    @Override
    public Map<String, List<OperationInvocation>> getMethodInvocationMap() {
        Map<String, List<OperationInvocation>> map = new LinkedHashMap<>();
        for (Expression expression : this.expressions) {
            Map<String, List<OperationInvocation>> expressionMap = expression.getMethodInvocationMap();
            for (String key : expressionMap.keySet()) {
                if (map.containsKey(key)) {
                    map.get(key).addAll(expressionMap.get(key));
                } else {
                    List<OperationInvocation> list = new ArrayList<>();
                    list.addAll(expressionMap.get(key));
                    map.put(key, list);
                }
            }
        }
        return map;
    }

    @Override
    public Map<String, List<ObjectCreation>> getCreationMap() {
        Map<String, List<ObjectCreation>> map = new LinkedHashMap<>();
        for (Expression expression : this.expressions) {
            Map<String, List<ObjectCreation>> expressionMap = expression.getCreationMap();
            for (String key : expressionMap.keySet()) {
                if (map.containsKey(key)) {
                    map.get(key).addAll(expressionMap.get(key));
                } else {
                    List<ObjectCreation> list = new ArrayList<>();
                    list.addAll(expressionMap.get(key));
                    map.put(key, list);
                }
            }
        }
        return map;
    }

    @Override
    public List<String> getStringLiterals() {
        List<String> stringLiterals = new ArrayList<>();
        for (Expression expression : this.expressions) {
            stringLiterals.addAll(expression.getStringLiterals());
        }
        return stringLiterals;
    }

    @Override
    public List<String> getNumberLiterals() {
        List<String> numberLiterals = new ArrayList<>();
        for (Expression expression : this.expressions) {
            numberLiterals.addAll(expression.getNumberLiterals());
        }
        return numberLiterals;
    }

    @Override
    public List<String> getNullLiterals() {
        List<String> nullLiterals = new ArrayList<>();
        for (Expression expression : this.expressions) {
            nullLiterals.addAll(expression.getNullLiterals());
        }
        return nullLiterals;
    }

    @Override
    public List<String> getBooleanLiterals() {
        List<String> booleanLiterals = new ArrayList<>();
        for (Expression expression : this.expressions) {
            booleanLiterals.addAll(expression.getBooleanLiterals());
        }
        return booleanLiterals;
    }

    @Override
    public List<String> getInfixOperators() {
        List<String> infixOperators = new ArrayList<>();
        for (Expression expression : this.expressions) {
            infixOperators.addAll(expression.getInfixOperators());
        }
        return infixOperators;
    }

    @Override
    public List<String> getArrayAccesses() {
        List<String> arrayAccesses = new ArrayList<>();
        for (Expression expression : this.expressions) {
            arrayAccesses.addAll(expression.getArrayAccesses());
        }
        return arrayAccesses;
    }

    @Override
    public List<String> getPrefixExpressions() {
        List<String> prefixExpressions = new ArrayList<>();
        for (Expression expression : this.expressions) {
            prefixExpressions.addAll(expression.getPrefixExpressions());
        }
        return prefixExpressions;
    }

    @Override
    public List<String> getPostfixExpressions() {
        List<String> postfixExpressions = new ArrayList<>();
        for (Expression expression : this.expressions) {
            postfixExpressions.addAll(expression.getPostfixExpressions());
        }
        return postfixExpressions;
    }

    @Override
    public List<TernaryOperatorExpression> getTernaryOperatorExpressions() {
        List<TernaryOperatorExpression> ternaryExpressions = new ArrayList<>();
        for (Expression expression : this.expressions) {
            ternaryExpressions.addAll(expression.getTernaryOperatorExpressions());
        }
        return ternaryExpressions;
    }

    @Override
    public List<String> getIdentifierArguments() {
        List<String> arguments = new ArrayList<String>();
        for (Expression expression : expressions) {
            arguments.addAll(expression.getIdentifierArguments());
        }
        return arguments;
    }

    @Override
    public List<VariableDeclaration> getVariableDeclarations() {
        List<VariableDeclaration> variableDeclarations = new ArrayList<>();
        //special handling for enhanced-for formal parameter
        // variableDeclarations.addAll(this.variableDeclarations);
        for (Expression expression : this.getExpressions()) {
            variableDeclarations.addAll(expression.getVariableDeclarations());
        }
        return variableDeclarations;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    /**
     * Check if this fragment contains the supplied fragment as its children or itself
     */
    public boolean containsFragment(CodeFragment fragment) {
        if (fragment == this)
            return true;

        if (fragment instanceof SingleStatement) {
            return getAllLeafStatementsIncludingNested().contains(fragment);
        } else if (fragment instanceof BlockStatement) {
            return getAllBlockStatementsIncludingNested().contains(fragment);
        } else if (fragment instanceof Expression) {
            return this.getExpressions().contains(fragment);
        }
        return false;
    }

    public List<VariableDeclaration> getAllVariableDeclarations() {
        List<VariableDeclaration> variableDeclarations = new ArrayList<>();
        variableDeclarations.addAll(getVariableDeclarations());
        for (Statement statement : statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                variableDeclarations.addAll(composite.getAllVariableDeclarations());
            } else if (statement instanceof SingleStatement) {
                SingleStatement statementObject = (SingleStatement) statement;
                variableDeclarations.addAll(statementObject.getVariableDeclarations());
//                for(LambdaExpressionObject lambda : statementObject.getLambdas()) {
//                    if(lambda.getBody() != null) {
//                        variableDeclarations.addAll(lambda.getBody().getAllVariableDeclarations());
//                    }
//                }
            }
        }
        return variableDeclarations;
    }

    public VariableDeclaration getVariableDeclaration(String variableName) {
        List<VariableDeclaration> variableDeclarations = getAllVariableDeclarations();
        for (VariableDeclaration declaration : variableDeclarations) {
            if (declaration.variableName.equals(variableName)) {
                return declaration;
            }
        }
        return null;
    }

    @Override
    public List<IFunctionDeclaration> getFunctionDeclarations() {
        List<IFunctionDeclaration> functions = new ArrayList<>();
        for (Expression expression : this.expressions) {
            functions.addAll(expression.getFunctionDeclarations());
        }
        return functions;
    }

    @Override
    public List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations() {
        List<IAnonymousFunctionDeclaration> anonymousFunctionDeclarations = new ArrayList<>();
        for (Expression expression : this.expressions) {
            anonymousFunctionDeclarations.addAll(expression.getAnonymousFunctionDeclarations());
        }
        return anonymousFunctionDeclarations;
    }


    public Map<String, List<OperationInvocation>> getAllMethodInvocationsIncludingNested() {
        Map<String, List<OperationInvocation>> map = new LinkedHashMap<>();
        map.putAll(getMethodInvocationMap());
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;

                Map<String, List<OperationInvocation>> compositeMap = composite.getAllMethodInvocationsIncludingNested();
                for (String key : compositeMap.keySet()) {
                    map.computeIfAbsent(key, call -> new ArrayList<>()).addAll(compositeMap.get(key));

//                    if (map.containsKey(key)) {
//                        map.get(key).addAll(compositeMap.get(key));
//                    } else {
//                        List<OperationInvocation> list = new ArrayList<>();
//                        list.addAll(compositeMap.get(key));
//                        map.put(key, list);
//                    }
                }
            } else if (statement instanceof SingleStatement) {
                SingleStatement statementObject = (SingleStatement) statement;
                Map<String, List<OperationInvocation>> statementMap = statementObject.getMethodInvocationMap();
                for (String key : statementMap.keySet()) {
                    map.computeIfAbsent(key, call -> new ArrayList<>()).addAll(statementMap.get(key));
                }
//
//                for (LambdaExpressionObject lambda : statementObject.getLambdas()) {
//                    if (lambda.getBody() != null) {
//                        Map<String, List<OperationInvocation>> lambdaMap = lambda.getBody().getCompositeStatement().getAllMethodInvocations();
//                        for (String key : lambdaMap.keySet()) {
//                            if (map.containsKey(key)) {
//                                map.get(key).addAll(lambdaMap.get(key));
//                            } else {
//                                List<OperationInvocation> list = new ArrayList<OperationInvocation>();
//                                list.addAll(lambdaMap.get(key));
//                                map.put(key, list);
//                            }
//                        }
//                    }
//                }
            }
        }
        return map;
    }

    public BlockStatement loopWithVariables(String currentElementName, String collectionName) {
        for (BlockStatement innerNode : getAllBlockStatementsIncludingNested()) {
            if (innerNode.getCodeElementType().equals(equals(CodeElementType.ENHANCED_FOR_STATEMENT))) {
                boolean currentElementNameMatched = false;
                for (VariableDeclaration declaration : innerNode.getVariableDeclarations()) {
                    if (declaration.getVariableName().equals(currentElementName)) {
                        currentElementNameMatched = true;
                        break;
                    }
                }
                boolean collectionNameMatched = false;
                for (Expression expression : innerNode.getExpressions()) {
                    if (expression.getVariables().contains(collectionName)) {
                        collectionNameMatched = true;
                        break;
                    }
                }
                if (currentElementNameMatched && collectionNameMatched) {
                    return innerNode;
                }
            } else if (innerNode.getCodeElementType().equals(CodeElementType.FOR_STATEMENT) ||
                    innerNode.getCodeElementType().equals(CodeElementType.WHILE_STATEMENT)) {
                boolean collectionNameMatched = false;
                for (Expression expression : innerNode.getExpressions()) {
                    if (expression.getVariables().contains(collectionName)) {
                        collectionNameMatched = true;
                        break;
                    }
                }
                boolean currentElementNameMatched = false;
                for (SingleStatement statement : innerNode.getAllLeafStatementsIncludingNested()) {
                    VariableDeclaration variableDeclaration = statement.getVariableDeclaration(currentElementName);
                    if (variableDeclaration != null && statement.getVariables().contains(collectionName)) {
                        currentElementNameMatched = true;
                        break;
                    }
                }
                if (currentElementNameMatched && collectionNameMatched) {
                    return innerNode;
                }
            }
        }
        return null;
    }

    public int statementCount() {
        int count = 0;
        if (!this.getText().equals("{"))
            count++;
        for (Statement statement : this.statements) {
            count += statement.statementCount();
        }
        return count;
    }

    public boolean contains(CodeFragment fragment) {
        if (fragment instanceof SingleStatement) {
            return getAllLeafStatementsIncludingNested().contains(fragment);
        } else if (fragment instanceof BlockStatement) {
            return getAllBlockStatementsIncludingNested().contains(fragment);
        } else if (fragment instanceof Expression) {
            return getExpressions().contains(fragment);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getText();
    }
}
