package io.rminer.core;

import io.rminer.core.api.IFragment;
import io.rminer.core.api.ISourceLocation;
import io.rminer.core.api.IVariableDeclaration;

import java.util.List;

public class Fragment implements IFragment {
    private String text;
    private ISourceLocation locationInfo;

    //private CompositeStatementObject owner;
    private List<String> variables;
    private List<String> types;

    private List<IVariableDeclaration> variableDeclarations;
    private Map<String, List<OperationInvocation>> methodInvocationMap;
    private List<AnonymousClassDeclarationObject> anonymousClassDeclarations;
    private List<String> stringLiterals;
    private List<String> numberLiterals;
    private List<String> nullLiterals;
    private List<String> booleanLiterals;
    private List<String> typeLiterals;
    private Map<String, List<ObjectCreation>> creationMap;
    private List<String> infixOperators;
    private List<String> arrayAccesses;
    private List<String> prefixExpressions;
    private List<String> postfixExpressions;
    private List<String> arguments;
    private List<TernaryOperatorExpression> ternaryOperatorExpressions;
    private List<LambdaExpressionObject> lambdas;
}

