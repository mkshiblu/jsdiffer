package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Invocation.InvocationCoverageType;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.SingleStatement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches invocation coverages as singleton
 */
public enum InvocationCoverage {
    INSTANCE;

    private final Map<OperationInvocation, InvocationCoverageType> invocationCoverageTypeMap = new HashMap<>();
    private final Map<SingleStatement, OperationInvocation> invocationCoveringEntireFragmentMap = new HashMap<>();

    public OperationInvocation getInvocationCoveringEntireFragment(SingleStatement statement) {
        if (!invocationCoveringEntireFragmentMap.containsKey(statement)) {
            OperationInvocation invocationCoveringEntireFragment = findInvocationCoveringEntireFragment(statement);
            invocationCoveringEntireFragmentMap.put(statement, invocationCoveringEntireFragment);
        }
        return invocationCoveringEntireFragmentMap.get(statement);
    }

    /**
     * Checks if the statement contains method invocation text covering the entire statement's text
     */
    private OperationInvocation findInvocationCoveringEntireFragment(SingleStatement statement) {
        Map<String, List<OperationInvocation>> methodInvocationMap = statement.getMethodInvocationMap();
        String statementText = statement.getText();
        InvocationCoverageType coveregeType = null;

        for (String invocationText : methodInvocationMap.keySet()) {
            List<OperationInvocation> invocations = methodInvocationMap.get(invocationText);

            for (OperationInvocation invocation : invocations) {
                if (invocationText.equals(statementText) || (invocationText + ";\n").equals(statementText)) {
                    coveregeType = InvocationCoverageType.ONLY_CALL;
                } else if (("return " + invocationText + ";\n").equals(statementText)) {
                    coveregeType = InvocationCoverageType.RETURN_CALL;
                } else if (isCastExpressionCoveringEntireFragment(invocationText)) {
                    coveregeType = InvocationCoverageType.CAST_CALL;
                } else if (expressionIsTheInitializerOfVariableDeclaration(invocationText)) {
                    coveregeType = InvocationCoverageType.VARIABLE_DECLARATION_INITIALIZER_CALL;
                } else if (invocation.getLocationInfo().getCodeElementType().equals(CodeElementType.SUPER_CONSTRUCTOR_INVOCATION) ||
                        invocation.getLocationInfo().getCodeElementType().equals(CodeElementType.CONSTRUCTOR_INVOCATION)) {
                    coveregeType = InvocationCoverageType.ONLY_CALL;
                }

                if (coveregeType != null) {
                    this.invocationCoverageTypeMap.put(invocation, coveregeType);
                    return invocation;
                }
            }
        }
        return null;
    }
}
