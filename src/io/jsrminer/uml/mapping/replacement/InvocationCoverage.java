package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.*;
import io.jsrminer.sourcetree.Invocation.InvocationCoverageType;

import java.util.*;

import static io.jsrminer.sourcetree.JsConfig.SINGLE_STATEMENT_TERMINATOR_CHAR;

/**
 * Caches invocation coverages as singleton
 */
public enum InvocationCoverage {
    INSTANCE;

    public final int CACHE_SIZE = 100;
    private final Map<OperationInvocation, InvocationCoverageType> invocationCoverageTypeMap = new HashMap<>();
    private final Map<SingleStatement, OperationInvocation> invocationCoveringEntireFragmentMap = new HashMap<>();

    public OperationInvocation getInvocationCoveringEntireFragment(SingleStatement statement) {
        if (!invocationCoveringEntireFragmentMap.containsKey(statement)) {
            OperationInvocation invocationCoveringEntireFragment = findInvocationCoveringEntireFragment(statement);
            updateCacheSize();
            invocationCoveringEntireFragmentMap.put(statement, invocationCoveringEntireFragment);
        }
        return invocationCoveringEntireFragmentMap.get(statement);
    }

    /**
     * Checks if the statement contains method invocation which text covers the entire statement's text
     */
    private OperationInvocation findInvocationCoveringEntireFragment(SingleStatement statement) {
        Map<String, List<OperationInvocation>> methodInvocationMap = statement.getMethodInvocationMap();
        String statementText = statement.getText();
        InvocationCoverageType coveregeType = null;

        for (String invocationText : methodInvocationMap.keySet()) {
            List<OperationInvocation> invocations = methodInvocationMap.get(invocationText);

            for (OperationInvocation invocation : invocations) {
                if (invocationText.equals(statementText) || (invocationText + SINGLE_STATEMENT_TERMINATOR_CHAR).equals(statementText)) {
                    coveregeType = InvocationCoverageType.ONLY_CALL;
                } else if (("return " + invocationText + SINGLE_STATEMENT_TERMINATOR_CHAR).equals(statementText)) {
                    coveregeType = InvocationCoverageType.RETURN_CALL;
                } else if (isCastExpressionCoveringEntireFragment(statementText, invocationText)) {
                    coveregeType = InvocationCoverageType.CAST_CALL;
                } else if (expressionIsTheInitializerOfVariableDeclaration(statement.getVariableDeclarations(), invocationText)) {
                    coveregeType = InvocationCoverageType.VARIABLE_DECLARATION_INITIALIZER_CALL;
                } else if (invocation.getType().equals(CodeElementType.SUPER_CONSTRUCTOR_INVOCATION) ||
                        invocation.getType().equals(CodeElementType.CONSTRUCTOR_INVOCATION)) {
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

    public OperationInvocation assignmentInvocationCoveringEntireStatement(SingleStatement statement) {
        Map<String, List<OperationInvocation>> methodInvocationMap = statement.getMethodInvocationMap();
        for (String methodInvocation : methodInvocationMap.keySet()) {
            List<OperationInvocation> invocations = methodInvocationMap.get(methodInvocation);
            for (OperationInvocation invocation : invocations) {
                if (expressionIsTheRightHandSideOfAssignment(methodInvocation, statement)) {
                    return invocation;
                }
            }
        }
        return null;
    }

    public ObjectCreation creationCoveringEntireFragment(SingleStatement statement) {
        Map<String, List<ObjectCreation>> creationMap = statement.getCreationMap();
        String text = statement.getText();
        InvocationCoverageType coveregeType = null;
        for (String objectCreation : creationMap.keySet()) {
            List<ObjectCreation> creations = creationMap.get(objectCreation);
            for (ObjectCreation creation : creations) {
                if ((objectCreation + SINGLE_STATEMENT_TERMINATOR_CHAR).equals(text) || objectCreation.equals(text)) {
                    coveregeType = InvocationCoverageType.ONLY_CALL;
                    return creation;
                } else if (("return " + objectCreation + SINGLE_STATEMENT_TERMINATOR_CHAR).equals(text)) {
                    coveregeType = InvocationCoverageType.RETURN_CALL;
                    return creation;
                } else if (("throw " + objectCreation + SINGLE_STATEMENT_TERMINATOR_CHAR).equals(text)) {
                    coveregeType = InvocationCoverageType.THROW_CALL;
                    return creation;
                } else if (expressionIsTheInitializerOfVariableDeclaration(statement.getVariableDeclarations(), objectCreation)) {
                    coveregeType = InvocationCoverageType.VARIABLE_DECLARATION_INITIALIZER_CALL;
                    return creation;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a cast expression covers the entire statement in return
     *
     * @return
     */
    private boolean isCastExpressionCoveringEntireFragment(String statementText, String expression) {
        int index = statementText.indexOf(expression + ";\n");
        if (index != -1) {
            String prefix = statementText.substring(0, index);
            if (prefix.contains("(") && prefix.contains(")")) {
                String casting = prefix.substring(prefix.indexOf("("), prefix.indexOf(")") + 1);
                if (("return " + casting + expression + ";\n").equals(statementText)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean expressionIsTheInitializerOfVariableDeclaration(Collection<VariableDeclaration> variableDeclarations, String expression) {
//        List<VariableDeclaration> variableDeclarations = getVariableDeclarations();
        Expression intializer;
        if (variableDeclarations.size() == 1 && (intializer = variableDeclarations.iterator().next().getInitializer()) != null) {
            String intializerText = intializer.getText();
            if (intializerText.equals(expression))
                return true;
            if (intializerText.startsWith("(")) {
                //ignore casting
                String initializerWithoutCasting = intializerText.substring(intializerText.indexOf(")") + 1/*, initializer.length()*/);
                if (initializerWithoutCasting.equals(expression))
                    return true;
            }
        }
        return false;
    }

    private boolean expressionIsTheRightHandSideOfAssignment(String expression, SingleStatement statement) {
        String statementText = statement.getText();
        if (statementText.contains("=")) {
            Set<String> variables = statement.getVariables();
            if (variables.size() > 0) {
                String s = variables.stream().findFirst().get() + " = " + expression + ";\n";
                if (statementText.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clearCache() {
        invocationCoverageTypeMap.clear();
        invocationCoveringEntireFragmentMap.clear();
    }

    public void updateCacheSize() {
        if (invocationCoveringEntireFragmentMap.size() >= CACHE_SIZE) {
            clearCache();
        }
    }


}
