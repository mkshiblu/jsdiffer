package io.jsrminer.uml;

import io.jsrminer.sourcetree.*;
import io.jsrminer.util.DiffUtil;
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


    public static boolean equalQualifiedName(IFunctionDeclaration function1
            , IFunctionDeclaration function2) {
        return function1.getQualifiedName() == function2.getQualifiedName();
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
                && functionDeclaration.getBody().blockStatement.getFunctionDeclarations().size() == 0
                && functionDeclaration.getAnonymousFunctionDeclarations().size() == 0
                && functionDeclaration.getFunctionDeclarations().size() == 0;
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

    public static boolean equalSignatureIgnoringChangedTypes(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        if (!(operation1.isConstructor() && operation2.isConstructor() || equivalentName(operation1, operation2)))
            return false;

//        if(operation1.isAbstract != operation.isAbstract)
//            return false;
        if (operation1.isStatic() != operation2.isStatic())
            return false;
		/*
		if(this.isFinal != operation.isFinal)
			return false;*/
        if (!equalParameterCount(operation1, operation2))
            return false;
//        if (!equalTypeParameters(operation))
//            return false;
        int i = 0;
        for (UMLParameter thisParameter : operation1.getParameters()) {
            UMLParameter otherParameter = operation2.getParameters().get(i);
            if (/*!thisParameter.equals(otherParameter)
                    &&*/ !parameterEqualsExcludingType(thisParameter, otherParameter))
                return false;
            i++;
        }
        return true;
    }

    public static boolean parameterEqualsExcludingType(UMLParameter parameter1, UMLParameter parameter2) {
        return parameter1.name.equals(parameter2.name);
        //&& parameter1.get.equals(parameter.kind);
    }

    private static boolean equivalentName(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        return nameEquals(operation1, operation2) || equivalentNames(operation1, operation2) || equivalentNames(operation2, operation1);
    }

    private static boolean equivalentNames(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        boolean equalReturn = true;//operation1.equalReturnParameter(operation2)
//                && operation1.getParametersWithoutReturnType().size() > 0
//                && operation2.getParametersWithoutReturnType().size() > 0;

        if (operation1.getName().startsWith(operation2.getName())
                && !operation2.getName().equals("get")
                && !operation2.getName().equals("set")
                && !operation2.getName().equals("print")) {
            String suffix1 = operation1.getName().substring(operation2.getName().length(), operation1.getName().length());
            String className2 = operation2.getParentContainerQualifiedName().contains(".")
                    ? operation2.getParentContainerQualifiedName().substring(operation2.getParentContainerQualifiedName().lastIndexOf(".") + 1, operation2.getParentContainerQualifiedName().length())
                    : operation2.getParentContainerQualifiedName();

            return operation2.getName().length() > operation1.getName().length() - operation2.getName().length()
                    || equalReturn
                    || className2.contains(suffix1);
        }
        return false;
    }

    public static boolean equalSignatureWithIdenticalNameIgnoringChangedTypes(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        if (!(operation1.isConstructor() && operation2.isConstructor() || operation1.getName().equals(operation2.getName())))
            return false;

//        if(operation1.isAbstract != operation.isAbstract)
//            return false;
        /*if(operation1.isStatic() != operation2.isStatic())
            return false;

		if(this.isFinal != operation.isFinal)
			return false;*/
        if (!equalParameterCount(operation1, operation2))
            return false;
//        if (!equalTypeParameters(operation))
//            return false;
        int i = 0;
        for (UMLParameter thisParameter : operation1.getParameters()) {
            UMLParameter otherParameter = operation2.getParameters().get(i);
            if (/*!thisParameter.equals(otherParameter)
                    &&*/ !parameterEqualsExcludingType(thisParameter, otherParameter))
                return false;
            i++;
        }
        return true;
    }

    public static boolean compatibleSignature(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        return overloadedParameterNames(operation1, operation2)
                || equalParameterNames(operation1, operation2)
                || isCommonParameterNamesMoreThanUncommon(operation1, operation2);
    }

    /**
     * Returns true if all paramters is operation1 is in Operation2 or vice versa
     */
    public static boolean overloadedParameterNames(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        return operation1.getParameterNameList().containsAll(operation2.getParameterNameList())
                || operation2.getParameterNameList().containsAll(operation1.getParameterNameList());
    }

    /**
     * Returns true if comman paramters count are more than
     */
    public static boolean isCommonParameterNamesMoreThanUncommon(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        var parameters1 = operation1.getParameterNameList();
        var parameters2 = operation2.getParameterNameList();
        var commonNames = DiffUtil.common(parameters1, parameters2);
        if (commonNames.isEmpty())
            return false;

        List<String> differentNames;
        if (parameters1.size() > parameters2.size())
            differentNames = DiffUtil.getUnmatchedInFirstCollection(parameters1, parameters2);
        else {
            differentNames = DiffUtil.getUnmatchedInFirstCollection(parameters2, parameters1);
        }

        return commonNames.size() >= differentNames.size();
    }
}
