package io.rminerx.core.entities;

import io.rminer.core.api.*;
import io.rminerx.core.api.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Fragment implements ICodeFragment {
    protected String text;
    protected ISourceLocation locationInfo;

    protected ICompositeFragment owner;
    protected List<String> variables = new ArrayList<>();
    protected List<String> types = new ArrayList<>();
    protected List<IVariableDeclaration> variableDeclarations = new ArrayList<>();
    protected List<IAnonymousClassDeclaration> anonymousClassDeclarations = new ArrayList<>();
    protected List<String> stringLiterals = new ArrayList<>();
    protected List<String> numberLiterals = new ArrayList<>();
    protected List<String> nullLiterals = new ArrayList<>();
    protected List<String> booleanLiterals = new ArrayList<>();
    protected List<String> infixOperators = new ArrayList<>();
    protected List<String> arrayAccesses = new ArrayList<>();
    protected List<String> typeLiterals = new ArrayList<>();
    protected List<String> prefixExpressions = new ArrayList<>();
    protected List<String> postfixExpressions = new ArrayList<>();
    protected List<ITernaryOperatorExpression> ternaryOperatorExpressions = new ArrayList<>();
    protected List<String> arguments = new ArrayList<>();
    protected Map<String, List<IOperationInvocation>> methodInvocationMap = new LinkedHashMap<>();
    protected Map<String, List<IObjectCreation>> creationMap = new LinkedHashMap<>();
    protected List<ILambdaExpression> lambdas = new ArrayList<>();
    protected List<IFunctionDeclaration> functionDeclarations = new ArrayList<>();

    @Override
    public List<? extends IFunctionDeclaration> getFunctionDeclarations() {
        return this.functionDeclarations;
    }
}
