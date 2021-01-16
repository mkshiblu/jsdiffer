package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.ContainerDiffer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO rename class
public class ReplacementHeuristic {

    CodeFragment statement1;
    CodeFragment statement2;
    ReplacementInfo replacementInfo;

    public ReplacementHeuristic(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo) {
        this.statement1 = statement1;
        this.statement2 = statement2;
        this.replacementInfo = replacementInfo;
    }

    public static boolean isIdenticalMethodInvocation(OperationInvocation assignmentInvocationCoveringTheEntireStatement1
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , ReplacementInfo replacementInfo
            , CodeFragment statement1, CodeFragment statement2) {

        final OperationInvocation invocationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement2);
        if (assignmentInvocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
            for (String key1 : methodInvocationMap1.keySet()) {
                for (Invocation invocation1 : methodInvocationMap1.get(key1)) {
                    if (invocation1.identical(invocationCoveringTheEntireStatement2,
                            replacementInfo.getReplacements()) &&
                            !assignmentInvocationCoveringTheEntireStatement1.getArguments().contains(key1)) {
                        String expression1 = assignmentInvocationCoveringTheEntireStatement1.getExpressionText();
                        if (expression1 == null || !expression1.contains(key1)) {
                            //   return replacementInfo.getReplacements();
                            return true;
                        }
                    } else if (invocation1.identicalName(invocationCoveringTheEntireStatement2) && invocation1.equalArguments(invocationCoveringTheEntireStatement2) &&
                            !assignmentInvocationCoveringTheEntireStatement1.getArguments().contains(key1) && invocationCoveringTheEntireStatement2.getExpressionText() != null) {
                        boolean expressionMatched = false;
                        Set<CodeFragment> additionallyMatchedStatements2 = new LinkedHashSet<>();
                        for (CodeFragment codeFragment : replacementInfo.unMatchedStatements2) {
                            VariableDeclaration variableDeclaration = codeFragment.getVariableDeclaration(invocationCoveringTheEntireStatement2.getExpressionText());
                            OperationInvocation invocationCoveringEntireCodeFragment = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(codeFragment);
                            if (variableDeclaration != null && variableDeclaration.getInitializer() != null && invocation1.getExpressionText() != null && invocation1.getExpressionText().equals(variableDeclaration.getInitializer().getText())) {
                                Replacement r = new Replacement(invocation1.getExpressionText(), variableDeclaration.getVariableName(), ReplacementType.VARIABLE_REPLACED_WITH_EXPRESSION_OF_METHOD_INVOCATION);
                                replacementInfo.getReplacements().add(r);
                                additionallyMatchedStatements2.add(codeFragment);
                                expressionMatched = true;
                            }
                            if (invocationCoveringEntireCodeFragment != null && assignmentInvocationCoveringTheEntireStatement1.identicalName(invocationCoveringEntireCodeFragment) &&
                                    assignmentInvocationCoveringTheEntireStatement1.equalArguments(invocationCoveringEntireCodeFragment)) {
                                additionallyMatchedStatements2.add(codeFragment);
                            }
                        }
                        if (expressionMatched) {
                            if (additionallyMatchedStatements2.size() > 0) {
                                Replacement r = new CompositeReplacement(statement1.getText(), statement2.getText(), new LinkedHashSet<>(), additionallyMatchedStatements2);
                                replacementInfo.getReplacements().add(r);
                            }
//                            return replacementInfo.getReplacements();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isObjectCreationReplacedWithArrayDeclaration(CodeFragment statement1, CodeFragment statement2
            , ReplacementInfo replacementInfo) {
        final ObjectCreation creationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement1);
        final ObjectCreation creationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement2);
        final List<VariableDeclaration> variableDeclarations1 = statement1.getVariableDeclarations();
        final List<VariableDeclaration> variableDeclarations2 = statement2.getVariableDeclarations();

        //check if array creation is replaced with data structure creation
        if (creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
                variableDeclarations1.size() == 1 && variableDeclarations2.size() == 1) {

            VariableDeclaration v1 = variableDeclarations1.get(0);
            VariableDeclaration v2 = variableDeclarations2.get(0);

            String initializer1 = v1.getInitializer() != null ? v1.getInitializer().getText() : null;
            String initializer2 = v2.getInitializer() != null ? v2.getInitializer().getText() : null;

//            Replacement r;

//            boolean isArrayCreationReplacedWithObjectCreation = (creationCoveringTheEntireStatement1.isArray()
//                    && !creationCoveringTheEntireStatement2.isArray());
//
//            boolean isObjectCreationReplacedWithArrayCreation = (creationCoveringTheEntireStatement2.isArray()
//                    && !creationCoveringTheEntireStatement1.isArray());
//
//            boolean sameArguments = false;
//
//            if (initializer1 != null && initializer2 != null) {
//                String arrayElements1 = initializer1
//                        .substring(initializer1.indexOf("[") + 1
//                                , initializer1.lastIndexOf("]"));
//                String objectCreationArguments1 = initializer2.substring(initializer2.indexOf("(") + 1,
//                        initializer2.lastIndexOf("]"));
//
//                sameArguments = arrayElements1.equals(objectCreationArguments1);
//            }

            boolean creation2IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement2.getFunctionName());
            boolean creation1IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement1.getFunctionName());

//            boolean creation1IsEmptyArray = "[]".equals(creationCoveringTheEntireStatement1.getText());
            //          boolean creation2IsEmptyArray = "[]".equals(creationCoveringTheEntireStatement2.getText());

            // Check if replaced With built int types;
            boolean arrayCreationReplacedWithArrayConstructor = creationCoveringTheEntireStatement1.isArray()
                    && creation2IsArrayConstructor;
            boolean arrayConstructorReplacedWithArrayCreation = creationCoveringTheEntireStatement2.isArray()
                    && creation1IsArrayConstructor;

            if (arrayConstructorReplacedWithArrayCreation || arrayCreationReplacedWithArrayConstructor) {
                Replacement replacement = new ObjectCreationReplacement(initializer1, initializer2,
                        creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, ReplacementType.ARRAY_CONSTRUCTOR_REPLACED_WITH_ARRAY_CREATION);
                replacementInfo.addReplacement(replacement);
                return true;
            }
        }
        return false;
    }

    public static boolean functionInvocationIsIdenticalWithADifferenceInExpression(CodeFragment statement1, CodeFragment statement2
            , ReplacementInfo replacementInfo, Map<String, List<? extends Invocation>> methodInvocationMap1, Map<String, List<? extends Invocation>> methodInvocationMap2) {
        final OperationInvocation invocationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement1);
        final OperationInvocation invocationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement2);

        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
            if (invocationCoveringTheEntireStatement1
                    .identicalWithExpressionCallChainDifference(invocationCoveringTheEntireStatement2)) {
                List<? extends Invocation> invokedOperationsBefore = methodInvocationMap1.get(invocationCoveringTheEntireStatement1.getExpressionText());
                List<? extends Invocation> invokedOperationsAfter = methodInvocationMap2.get(invocationCoveringTheEntireStatement2.getExpressionText());
                if (invokedOperationsBefore != null && invokedOperationsBefore.size() > 0 && invokedOperationsAfter != null && invokedOperationsAfter.size() > 0) {
                    OperationInvocation invokedOperationBefore = (OperationInvocation) invokedOperationsBefore.get(0);
                    OperationInvocation invokedOperationAfter = (OperationInvocation) invokedOperationsAfter.get(0);
                    Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getExpressionText(), invocationCoveringTheEntireStatement2.getExpressionText(), invokedOperationBefore, invokedOperationAfter, ReplacementType.METHOD_INVOCATION_EXPRESSION);
                    replacementInfo.addReplacement(replacement);
                    //return replacementInfo.getReplacements();
                    return true;
                } else if (invokedOperationsBefore != null && invokedOperationsBefore.size() > 0) {
                    OperationInvocation invokedOperationBefore = (OperationInvocation) invokedOperationsBefore.get(0);
                    Replacement replacement = new VariableReplacementWithMethodInvocation(invocationCoveringTheEntireStatement1.getExpressionText(), invocationCoveringTheEntireStatement2.getExpressionText(), invokedOperationBefore, VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                    replacementInfo.addReplacement(replacement);
                    //return replacementInfo.getReplacements();
                    return true;
                } else if (invokedOperationsAfter != null && invokedOperationsAfter.size() > 0) {
                    OperationInvocation invokedOperationAfter = (OperationInvocation) invokedOperationsAfter.get(0);
                    Replacement replacement = new VariableReplacementWithMethodInvocation(invocationCoveringTheEntireStatement1.getExpressionText(), invocationCoveringTheEntireStatement2.getExpressionText(), invokedOperationAfter, VariableReplacementWithMethodInvocation.Direction.VARIABLE_TO_INVOCATION);
                    replacementInfo.addReplacement(replacement);
                    //return replacementInfo.getReplacements();
                    return true;
                }
                if (invocationCoveringTheEntireStatement1.numberOfSubExpressions() == invocationCoveringTheEntireStatement2.numberOfSubExpressions() &&
                        invocationCoveringTheEntireStatement1.getExpressionText().contains(".") == invocationCoveringTheEntireStatement2.getExpressionText().contains(".")) {
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean invocationIsIdenticalIfArgumentsAreReplaced(CodeFragment statement1
            , CodeFragment statement2, ReplacementInfo replacementInfo
            , OperationInvocation invocationCoveringTheEntireStatement1, OperationInvocation invocationCoveringTheEntireStatement2,
                                                                      Map<String, List<? extends Invocation>> methodInvocationMap2) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                invocationCoveringTheEntireStatement1.identicalExpression(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) &&
                invocationCoveringTheEntireStatement1.identicalName(invocationCoveringTheEntireStatement2)) {
            for (String key : methodInvocationMap2.keySet()) {
                for (Invocation invocation2 : methodInvocationMap2.get(key)) {
                    if (invocationCoveringTheEntireStatement1.identicalOrReplacedArguments(invocation2, replacementInfo.getReplacements())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean invationisIdenticalIfArgumentsArWrappedOrConcatenateed(OperationInvocation invocationCoveringTheEntireStatement1,
                                                                          OperationInvocation invocationCoveringTheEntireStatement2,
                                                                          Map<String, List<? extends Invocation>> methodInvocationMap2) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                invocationCoveringTheEntireStatement1.identicalExpression(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) &&
                invocationCoveringTheEntireStatement1.identicalName(invocationCoveringTheEntireStatement2)) {
            for (String key : methodInvocationMap2.keySet()) {
                for (Invocation invocation2 : methodInvocationMap2.get(key)) {
                    if (invocationCoveringTheEntireStatement1.identicalOrWrappedArguments(invocation2)) {
                        Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                                invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT_WRAPPED);
                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
                        return true;
                    }
                    if (invocationCoveringTheEntireStatement1.identicalOrConcatenatedArguments(invocation2)) {
                        Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                                invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT_CONCATENATED);
                        replacementInfo.addReplacement(replacement);
                        //return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean invocationRenamedButIdenticalExpressionAndArguments(OperationInvocation invocationCoveringTheEntireStatement1,
                                                                       OperationInvocation invocationCoveringTheEntireStatement2) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                invocationCoveringTheEntireStatement1.renamedWithIdenticalExpressionAndArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()
                        , ContainerDiffer.MAX_OPERATION_NAME_DISTANCE)) {
            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getFunctionName(),
                    invocationCoveringTheEntireStatement2.getFunctionName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
            replacementInfo.addReplacement(replacement);
            return true;
        }
        return false;
    }
}
