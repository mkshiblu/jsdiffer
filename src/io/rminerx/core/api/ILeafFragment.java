package io.rminerx.core.api;

import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.TernaryOperatorExpression;
import io.jsrminer.sourcetree.VariableDeclaration;

import java.util.List;
import java.util.Map;

public interface ILeafFragment extends ICodeFragment {
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
    List<String> getIdentifierArguments();
    List<VariableDeclaration> getVariableDeclarations();
    VariableDeclaration getVariableDeclaration(String variableName);
    VariableDeclaration findVariableDeclarationIncludingParent(String variableName);
    List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations();
//    List<IFunctionDeclaration> getFunctionDeclarations();
}
