package io.rminerx.core.api;

import io.jsrminer.sourcetree.*;

import java.util.List;
import java.util.Map;

public interface ILeafFragment extends ICodeFragment {
    BlockStatement getParent();

    List<String> getVariables();

    Map<String, List<OperationInvocation>> getMethodInvocationMap();

    Map<String, List<ObjectCreation>> getCreationMap();

    List<String> getStringLiterals();

    List<String> getNumberLiterals();

    List<String> getNullLiterals();

    List<String> getBooleanLiterals();

    List<String> getInfixExpressions();

    List<String> getInfixOperators();

    List<String> getArrayAccesses();

    List<String> getPrefixExpressions();

    List<String> getPostfixExpressions();

    List<TernaryOperatorExpression> getTernaryOperatorExpressions();

    List<String> getArguments();

    List<VariableDeclaration> getVariableDeclarations();

    VariableDeclaration getVariableDeclaration(String variableName);

    VariableDeclaration findVariableDeclarationIncludingParent(String variableName);

    List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations();

    List<IAnonymousClassDeclaration> getAnonymousClassDeclarations();

    void registerAnonymousFunctionDeclaration(IAnonymousFunctionDeclaration anonymousFunctionDeclaration);

    void registerVariable(String name);

    void registerInfixOperator(String operator);

    void registerInfixExpression(String expression);

    void registerPrefixExpression(String expression);

    void registerPostfixExpression(String expression);
}
