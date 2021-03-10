package io.jsrminer.uml;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.StringDistance;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Contains helper for function signature matching
 */
public class FunctionUtil {
    public static boolean isLoop(BlockStatement blockStatement) {
        return blockStatement.getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT) ||
                blockStatement.getCodeElementType().equals(CodeElementType.FOR_STATEMENT) ||
                blockStatement.getCodeElementType().equals(CodeElementType.WHILE_STATEMENT) ||
                blockStatement.getCodeElementType().equals(CodeElementType.DO_WHILE_STATEMENT);
    }

    public static boolean isEqualFullyQualifiedParentContainerName(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return (function1.getSourceLocation().getFilePath() + "|" + function1.getParentContainerQualifiedName())
                .equals(function2.getSourceLocation().getFilePath() + "|" + function2.getParentContainerQualifiedName());
    }

    /**
     * Returns true if all these are eaual of two IsEmptyBody, ParentContainerQualifiedName, name ignored case,
     * and parameter names and count are same in order
     *
     * @return
     */
    public static boolean isEqualIgnoringNameCase(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return function1.getParentContainerQualifiedName().equals(function2.getParentContainerQualifiedName())
                && nameEqualsIgnoreCase(function1, function2)
                && (isEmptyBody(function1) == isEmptyBody(function2))
                && equalParameterNames(function1, function2);
    }

    /**
     * Returns true if all these are eaual of two IsEmptyBody, ParentContainerQualifiedName, name,
     * and parameter names and count are same in order
     *
     * @return
     */
    public static boolean isEqual(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return function1.getParentContainerQualifiedName().equals(function2.getParentContainerQualifiedName())
                && nameEquals(function1, function2)
                && (isEmptyBody(function1) == isEmptyBody(function2))
                && equalParameterNames(function1, function2);
    }

    /**
     * Equals if: containerQualifiedName, name Equals, Equals ParamsCount & names
     * Simiar to RM equalsQualified which checks the types of paramters in order
     */
    public static boolean equalsNameParentQualifiedNameAndParamerNames(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {

        return nameEquals(function1, function2)
                && equalsQualifiedParentContainerName(function1, function2)
                && equalParameterNames(function1, function2);
    }

    public static boolean equalsQualifiedParentContainerName(IFunctionDeclaration function1,
                                                             IFunctionDeclaration function2) {
        return function1.getParentContainerQualifiedName().equals(function2.getParentContainerQualifiedName());
    }

    public static boolean nameEquals(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return function1.getName().equals(function2.getName());
    }

    public static boolean nameEqualsIgnoreCase(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return function1.getName().equalsIgnoreCase(function2.getName());
    }

    public static boolean nameEqualsIgnoreCaseAndEqualParameterCount(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return nameEqualsIgnoreCase(function1, function2) && equalParameterCount(function1, function2);
    }

    /**
     * Returns true if the body does not contain any statmeent
     *
     * @return
     */
    public static boolean isEmptyBody(IFunctionDeclaration functionDeclaration) {
        return functionDeclaration.getBody().blockStatement.getStatements().size() == 0
                && functionDeclaration.getBody().blockStatement.getFunctionDeclarations().size() == 0;
    }

    /**
     * Returns true if qualified name and parameters count match
     *
     * @return
     */
    public static boolean equalsQualifiedNameAndParameterCount(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return equalParameterCount(function1, function2) &&
                function1.getQualifiedName().equals(function2.getQualifiedName());
    }

    /**
     * Returns true if name & parameter names and count are same in order
     */
    public static boolean equalNameAndParameterNames(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return function1.getName().equals(function2.getName())
                && equalParameterNames(function1, function2);
    }


    /**
     * Returns true if name & parameter count are same
     */
    public static boolean equalNameAndParameterCount(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return function1.getName().equals(function2.getName())
                && equalParameterCount(function1, function2);
    }

    /**
     * Returns true if name & parameter count and all param names are same and same order
     */
    public static boolean isExactSignature(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return function1.getName().equals(function2.getName())
                && equalParameterNames(function1, function2);
    }

    /**
     * @return True if parameter count and names are same in order
     */
    public static boolean equalParameterNames(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return equalParameterCount(function1, function2)
                && function1.getParameterNameList().equals(function2.getParameterNameList());
    }

    public static boolean equalParameterCount(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return function1.getParameters().size() == function2.getParameters().size();
    }

    public static boolean equalNameAndParameterNamesIgnoringOrder(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        return function1.getName().equals(function2.getName())
                && new LinkedHashSet<>(function1.getParameterNameList()).equals(new LinkedHashSet<>(function2.getParameterNameList()));
    }

    public static boolean isInvocationsEqual(OperationInvocation invocation1, OperationInvocation invocation2) {
        return invocation1.getName().equals(invocation2.getName())
                && invocation1.getArguments().size() == invocation2.getArguments().size();
    }

    public static boolean invocationsHaveEqualFunctionNames(OperationInvocation invocation1, OperationInvocation invocation2) {
        return invocation1.getName().equals(invocation2.getName());
    }

    public static boolean isDirectlyNested(IAnonymousFunctionDeclaration anonymousFunctionDeclaration) {
        return !anonymousFunctionDeclaration.getQualifiedName().contains(".");
    }

    public static OperationInvocation isDelegate(FunctionDeclaration functionDeclaration) {
        if (functionDeclaration.getBody() != null) {
            List<Statement> statements = functionDeclaration.getBody().blockStatement.getStatements();
            if (statements.size() == 1 && statements.get(0) instanceof SingleStatement) {
                SingleStatement statement = (SingleStatement) statements.get(0);
                Map<String, List<OperationInvocation>> operationInvocationMap = statement.getMethodInvocationMap();
                for (String key : operationInvocationMap.keySet()) {
                    List<OperationInvocation> operationInvocations = operationInvocationMap.get(key);
                    for (OperationInvocation operationInvocation : operationInvocations) {
                        if (operationInvocation.matchesOperation(functionDeclaration/*, this.variableTypeMap(), null*/)
                                || operationInvocation.getName().equals(functionDeclaration.getName())) {
                            return operationInvocation;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean containsInvocation(FunctionDeclaration originalOperation, OperationInvocation addedOperationInvocation) {
        for (OperationInvocation invocation : originalOperation.getBody().getAllOperationInvocations()) {
            if (invocation.getName().equals(addedOperationInvocation.getName()) &&
                    invocation.getArguments().size() == addedOperationInvocation.getArguments().size()) {
                return true;
            }
        }

        return false;
    }

    public static boolean equalTopLevelAnonymousFunctionDeclarationCount(IContainer container1, IContainer container2) {
        return container1.getAnonymousFunctionDeclarations().size() == container2.getAnonymousFunctionDeclarations().size();
    }
}
