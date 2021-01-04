package io.rminer.core.entities;

import io.rminer.core.api.*;

import java.util.List;
import java.util.Map;

public abstract class Fragment implements ICodeFragment {
    private String text;
    private ISourceLocation locationInfo;

    //private CompositeStatementObject owner;
    private List<String> variables;
    private List<String> types;

    private List<IVariableDeclaration> variableDeclarations;
    private Map<String, List<IOperationInvocation>> methodInvocationMap;
    private List<IAnonymousClassDeclaration> anonymousClassDeclarations;
    private List<String> stringLiterals;
    private List<String> numberLiterals;
    private List<String> nullLiterals;
    private List<String> booleanLiterals;
    private List<String> typeLiterals;
    private Map<String, List<IObjectCreation>> creationMap;
    private List<String> infixOperators;
    private List<String> arrayAccesses;
    private List<String> prefixExpressions;
    private List<String> postfixExpressions;
    private List<String> arguments;
    private List<ITernaryOperatorExpression> ternaryOperatorExpressions;
    private List<ILambdaExpression> lambdas;
}

