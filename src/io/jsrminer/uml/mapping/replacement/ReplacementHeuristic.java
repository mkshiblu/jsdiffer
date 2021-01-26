package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.SourceFileDiffer;
import io.jsrminer.uml.mapping.FunctionBodyMapper;

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

            boolean creation2IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement2.getName());
            boolean creation1IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement1.getName());

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

    public boolean invasionsIdenticalIfArgumentsAreWrappedOrConcatenated(OperationInvocation invocationCoveringTheEntireStatement1,
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
                        , SourceFileDiffer.MAX_OPERATION_NAME_DISTANCE)) {
            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getName(),
                    invocationCoveringTheEntireStatement2.getName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
            replacementInfo.addReplacement(replacement);
            return true;
        }
        return false;
    }

    public boolean invocationRenamedButNoExpressionAndIdenticalArguments(OperationInvocation invocationCoveringTheEntireStatement1, OperationInvocation invocationCoveringTheEntireStatement2, List<FunctionBodyMapper> lambdaMappers) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                invocationCoveringTheEntireStatement1.renamedWithIdenticalArgumentsAndNoExpression(invocationCoveringTheEntireStatement2, SourceFileDiffer.MAX_OPERATION_NAME_DISTANCE, lambdaMappers)) {
            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getName(),
                    invocationCoveringTheEntireStatement2.getName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
            replacementInfo.addReplacement(replacement);
            return true;
        }
        return false;
    }

    public boolean invocationRenamedOneExpressionIsNullAndidenticalArguments(OperationInvocation invocationCoveringTheEntireStatement1, OperationInvocation invocationCoveringTheEntireStatement2) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                invocationCoveringTheEntireStatement1.renamedWithDifferentExpressionAndIdenticalArguments(invocationCoveringTheEntireStatement2)) {
            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                    invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME_AND_EXPRESSION);
            replacementInfo.addReplacement(replacement);
            //return replacementInfo.getReplacements();
            return true;
        }
        return false;
    }

    public boolean invocationRenmadeArgumentsChangedButIdenticalExpressions(OperationInvocation invocationCoveringTheEntireStatement1, OperationInvocation invocationCoveringTheEntireStatement2, List<FunctionBodyMapper> lambdaMappers) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                invocationCoveringTheEntireStatement1.renamedWithIdenticalExpressionAndDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), SourceFileDiffer.MAX_OPERATION_NAME_DISTANCE, lambdaMappers)) {
            ReplacementType type = invocationCoveringTheEntireStatement1.getName().equals(invocationCoveringTheEntireStatement2.getName()) ? ReplacementType.METHOD_INVOCATION_ARGUMENT : ReplacementType.METHOD_INVOCATION_NAME_AND_ARGUMENT;
            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                    invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, type);
            replacementInfo.addReplacement(replacement);
            //           return replacementInfo.getReplacements();
            return true;
        }
        return false;
    }

    public boolean invocationHasDifferentArgumentsCount(OperationInvocation invocationCoveringTheEntireStatement1
            , OperationInvocation invocationCoveringTheEntireStatement2
            , Map<String, String> parameterToArgumentMap
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , Set<String> methodInvocations1) {
        if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
            if (invocationCoveringTheEntireStatement1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
                //return replacementInfo.getReplacements();
                return true;
            } else if (invocationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
                Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                        invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT);
                replacementInfo.addReplacement(replacement);
                //    return replacementInfo.getReplacements();
                return true;
            }
        }


        if (!methodInvocations1.isEmpty() && invocationCoveringTheEntireStatement2 != null) {
            for (String methodInvocation1 : methodInvocations1) {
                for (Invocation operationInvocation1 : methodInvocationMap1.get(methodInvocation1)) {
                    if (operationInvocation1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
                        //return replacementInfo.getReplacements();
                        return true;
                    } else if (operationInvocation1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
                        Replacement replacement = new MethodInvocationReplacement(operationInvocation1.actualString(),
                                invocationCoveringTheEntireStatement2.actualString(), (OperationInvocation) operationInvocation1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT);
                        replacementInfo.addReplacement(replacement);
                        //return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean argumentIsReturnedInSecondStatement(OperationInvocation invocationCoveringTheEntireStatement1, Map<String, List<? extends Invocation>> methodInvocationMap1, Set<String> methodInvocations1) {
        //        //check if the argument of the method call in the first statement is returned in the second statement
        Replacement r;
        if (invocationCoveringTheEntireStatement1 != null
                && (r = invocationCoveringTheEntireStatement1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
            replacementInfo.addReplacement(r);
            //   return replacementInfo.getReplacements();
            return true;
        }
        for (String methodInvocation1 : methodInvocations1) {
            for (Invocation operationInvocation1 : methodInvocationMap1.get(methodInvocation1)) {
                if (statement1.getText().endsWith(methodInvocation1 + JsConfig.STATEMENT_TERMINATOR_CHAR)
                        && (r = operationInvocation1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
                    if (operationInvocation1.makeReplacementForReturnedArgument(statement2.getText()) != null) {
                        replacementInfo.addReplacement(r);
                        //            return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean argumentIsReturnedInFirstStatment(OperationInvocation invocationCoveringTheEntireStatement2,
                                                     Map<String, List<? extends Invocation>> methodInvocationMap2, Set<String> methodInvocations2) {
        Replacement r;
        if (invocationCoveringTheEntireStatement2 != null &&
                (r = invocationCoveringTheEntireStatement2.makeReplacementForWrappedCall(replacementInfo.getArgumentizedString1())) != null) {
            replacementInfo.addReplacement(r);
            //return replacementInfo.getReplacements();
            return true;
        }
        for (String methodInvocation2 : methodInvocations2) {
            for (Invocation operationInvocation2 : methodInvocationMap2.get(methodInvocation2)) {
                if (statement2.getText().endsWith(methodInvocation2 + JsConfig.STATEMENT_TERMINATOR_CHAR) && (r = operationInvocation2.makeReplacementForWrappedCall(replacementInfo.getArgumentizedString1())) != null) {
                    if (operationInvocation2.makeReplacementForWrappedCall(statement1.getText()) != null) {
                        replacementInfo.addReplacement(r);
                        //   return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean argumentIsOnRightHandSideInSecondStatement(OperationInvocation invocationCoveringTheEntireStatement2,
                                                              Map<String, List<? extends Invocation>> methodInvocationMap1) {
        Replacement r;
        if (invocationCoveringTheEntireStatement2 != null &&
                (r = invocationCoveringTheEntireStatement2.makeReplacementForAssignedArgument(replacementInfo.getArgumentizedString1())) != null &&
                methodInvocationMap1.containsKey(invocationCoveringTheEntireStatement2.getArguments().get(0))) {
            replacementInfo.addReplacement(r);
            //return replacementInfo.getReplacements();
            return true;
        }
        return false;
    }

    public boolean invocatioInSecondIsExpressionOfInvocationInFirst(OperationInvocation invocationCoveringTheEntireStatement2, Map<String, List<? extends Invocation>> methodInvocationMap1, Map<String, List<? extends Invocation>> methodInvocationMap2) {
        if (invocationCoveringTheEntireStatement2 != null) {
            for (String key1 : methodInvocationMap1.keySet()) {
                for (Invocation invocation1 : methodInvocationMap1.get(key1)) {
                    if (statement1.getText().endsWith(key1 + JsConfig.STATEMENT_TERMINATOR_CHAR) &&
                            methodInvocationMap2.keySet().contains(invocation1.getExpressionText())) {
                        Replacement replacement = new MethodInvocationReplacement(invocation1.actualString(),
                                invocationCoveringTheEntireStatement2.actualString(), (OperationInvocation) invocation1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION);
                        replacementInfo.addReplacement(replacement);
                        //return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean invocationInFirstIsExpressionOfInvocationInSecond(OperationInvocation invocationCoveringTheEntireStatement1, Map<String, List<? extends Invocation>> methodInvocationMap1, Map<String, List<? extends Invocation>> methodInvocationMap2) {
        if (invocationCoveringTheEntireStatement1 != null) {
            for (String key2 : methodInvocationMap2.keySet()) {
                for (Invocation invocation2 : methodInvocationMap2.get(key2)) {
                    if (statement2.getText().endsWith(key2 + JsConfig.STATEMENT_TERMINATOR_CHAR) &&
                            methodInvocationMap1.keySet().contains(invocation2.getExpressionText())) {
                        Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                                invocation2.actualString(), invocationCoveringTheEntireStatement1, (OperationInvocation) invocation2, ReplacementType.METHOD_INVOCATION);
                        replacementInfo.addReplacement(replacement);
                        //return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean argumentOfCreationIsExpressionOfInvocationInSecond(ObjectCreation creationCoveringTheEntireStatement1, Map<String, List<? extends Invocation>> methodInvocationMap2) {
        if (creationCoveringTheEntireStatement1 != null) {
            for (String key2 : methodInvocationMap2.keySet()) {
                for (Invocation invocation2 : methodInvocationMap2.get(key2)) {
                    if (statement2.getText().endsWith(key2 + JsConfig.STATEMENT_TERMINATOR_CHAR) &&
                            creationCoveringTheEntireStatement1.getArguments().contains(invocation2.getExpressionText())) {
                        Replacement replacement = new ClassInstanceCreationWithMethodInvocationReplacement(creationCoveringTheEntireStatement1.getName(),
                                invocation2.getName(), ReplacementType.CLASS_INSTANCE_CREATION_REPLACED_WITH_METHOD_INVOCATION, creationCoveringTheEntireStatement1, (OperationInvocation) invocation2);
                        replacementInfo.addReplacement(replacement);
                        //return replacementInfo.getReplacements();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean builderCallChainReplacedWithClassInstanceCreation(OperationInvocation invocationCoveringTheEntireStatement1, ObjectCreation creationCoveringTheEntireStatement2, Map<String, List<? extends Invocation>> methodInvocationMap1) {
        if (invocationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null) {
            if (invocationCoveringTheEntireStatement1.getName().equals("build")) {
                int commonArguments = 0;
                for (String key1 : methodInvocationMap1.keySet()) {
                    if (invocationCoveringTheEntireStatement1.actualString().startsWith(key1)) {
                        for (Invocation invocation1 : methodInvocationMap1.get(key1)) {
                            Set<String> argumentIntersection = invocation1.argumentIntersection(creationCoveringTheEntireStatement2);
                            commonArguments += argumentIntersection.size();
                        }
                    }
                }
                if (commonArguments > 0) {
                    Replacement replacement = new MethodInvocationWithClassInstanceCreationReplacement(invocationCoveringTheEntireStatement1.getName(),
                            creationCoveringTheEntireStatement2.getName(), ReplacementType.BUILDER_REPLACED_WITH_CLASS_INSTANCE_CREATION, invocationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2);
                    replacementInfo.addReplacement(replacement);
//                    return replacementInfo.getReplacements();
                    return true;
                }
            }
        }
        return false;
    }
//    public boolean identicalObjectCreations(ObjectCreation creationCoveringTheEntireStatement1, ObjectCreation creationCoveringTheEntireStatement2, Map<String, List<? extends Invocation>> methodInvocationMap1) {
//        if (creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
//                creationCoveringTheEntireStatement1.identical(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//            boolean identicalArrayInitializer = true;
//            if (creationCoveringTheEntireStatement1.isArray() && creationCoveringTheEntireStatement2.isArray()) {
//                identicalArrayInitializer = creationCoveringTheEntireStatement1.identicalArrayInitializer(creationCoveringTheEntireStatement2);
//            }
//            if (identicalArrayInitializer) {
//                //return replacementInfo.getReplacements();
//                return true;
//            }
//        }
//        return false;
//    }


    public boolean objectCreationIsDifferentOnlyByArguments(ObjectCreation creationCoveringTheEntireStatement1, ObjectCreation creationCoveringTheEntireStatement2, Map<String, String> parameterToArgumentMap) {
        if (creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null) {
            if (creationCoveringTheEntireStatement1.identicalWithMergedArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//                return replacementInfo.getReplacements();
                return true;
            } else if (creationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
                Replacement replacement = new ObjectCreationReplacement(creationCoveringTheEntireStatement1.getName(),
                        creationCoveringTheEntireStatement2.getName(), creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, ReplacementType.CLASS_INSTANCE_CREATION_ARGUMENT);
                replacementInfo.addReplacement(replacement);
                //return replacementInfo.getReplacements();
                return true;
            }
        }
        return false;
    }

    public boolean creationArgumentsAreIdenticalAfterReplacements(ObjectCreation creationCoveringTheEntireStatement1, ObjectCreation creationCoveringTheEntireStatement2, String s1, String s2) {
        if (creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
                creationCoveringTheEntireStatement1.identicalName(creationCoveringTheEntireStatement2) &&
                creationCoveringTheEntireStatement1.identicalExpression(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {

            if (creationCoveringTheEntireStatement1.isArray() && creationCoveringTheEntireStatement2.isArray()) {
                String subStringS1 = s1.substring(s1.indexOf("[") + 1, s1.lastIndexOf("]"));
                if (subStringS1.length() > 0 && subStringS1.equals(s2.substring(s2.indexOf("[") + 1, s2.lastIndexOf("]")))) {
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }

            if (!creationCoveringTheEntireStatement1.isArray() && !creationCoveringTheEntireStatement2.isArray()) {
                String subString2 = s1.substring(s1.indexOf("(") + 1, s1.lastIndexOf(")"));
                if (subString2.length() > 0 &&
                        subString2.equals(s2.substring(s2.indexOf("(") + 1, s2.lastIndexOf(")")))) {
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addtionalCreationHeuristics(Set<String> creations1
            , Map<String, List<? extends Invocation>> creationMap1
            , ObjectCreation creationCoveringTheEntireStatement1
            , ObjectCreation creationCoveringTheEntireStatement2
            , String s1, String s2
            , Map<String, String> parameterToArgumentMap) {

        Replacement r;
        if (!creations1.isEmpty() && creationCoveringTheEntireStatement2 != null) {
            for (String creation1 : creations1) {
                for (Invocation objectCreation1 : creationMap1.get(creation1)) {
                    if (objectCreation1.identicalWithMergedArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
                        //return replacementInfo.getReplacements();
                        return true;
                    } else if (objectCreation1.identicalWithDifferentNumberOfArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
                        Replacement replacement = new ObjectCreationReplacement(objectCreation1.getName(),
                                creationCoveringTheEntireStatement2.getName(), (ObjectCreation) objectCreation1, creationCoveringTheEntireStatement2, ReplacementType.CLASS_INSTANCE_CREATION_ARGUMENT);
                        replacementInfo.addReplacement(replacement);
                        //return replacementInfo.getReplacements();
                        return true;
                    }
                    //check if the argument lists are identical after replacements
                    if (objectCreation1.identicalName(creationCoveringTheEntireStatement2) &&
                            objectCreation1.identicalExpression(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {

                        if (((ObjectCreation) objectCreation1).isArray() && creationCoveringTheEntireStatement2.isArray()) {
                            String substring1 = s1.substring(s1.indexOf("[") + 1, s1.lastIndexOf("]"));
                            if (substring1.length() > 0 && substring1.equals(s2.substring(s2.indexOf("[") + 1, s2.lastIndexOf("]")))) {
                                //return replacementInfo.getReplacements();
                                return true;
                            }
                        }

                        if (!((ObjectCreation) objectCreation1).isArray() && !creationCoveringTheEntireStatement2.isArray()) {
                            String substring2 = s1.substring(s1.indexOf("(") + 1, s1.lastIndexOf(")"));
                            if (substring2.length() > 0 &&
                                    substring2.equals(s2.substring(s2.indexOf("(") + 1, s2.lastIndexOf(")")))
                            ) {
                                //return replacementInfo.getReplacements();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (creationCoveringTheEntireStatement1 != null && (r = creationCoveringTheEntireStatement1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
            replacementInfo.addReplacement(r);
            //return replacementInfo.getReplacements();
            return true;
        }
        for (String creation1 : creations1) {
            for (Invocation objectCreation1 : creationMap1.get(creation1)) {
                if (statement1.getText().endsWith(creation1 + JsConfig.STATEMENT_TERMINATOR_CHAR) && (r = objectCreation1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
                    replacementInfo.addReplacement(r);
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean variableDeclarationsHeuristics(String s1, String s2
            , OperationInvocation invocationCoveringTheEntireStatement1
            , OperationInvocation invocationCoveringTheEntireStatement2) {
        VariableDeclaration variableDeclarationWithArrayInitializer1 = ReplacementFinder.findDeclarationWithArrayInitializer(statement1.getVariableDeclarations());
        VariableDeclaration variableDeclarationWithArrayInitializer2 = ReplacementFinder.findDeclarationWithArrayInitializer(statement2.getVariableDeclarations());

        List<VariableDeclaration> variableDeclarations1 = statement1.getVariableDeclarations();
        List<VariableDeclaration> variableDeclarations2 = statement2.getVariableDeclarations();

        Replacement r;
        if (variableDeclarationWithArrayInitializer1 != null
                && invocationCoveringTheEntireStatement2 != null
                && variableDeclarations2.isEmpty() &&
                !ReplacementFinder.containsMethodSignatureOfAnonymousClass(statement1.getText())
                && !ReplacementFinder.containsMethodSignatureOfAnonymousClass(statement2.getText())) {
            String args1 = s1.substring(s1.indexOf("{") + 1, s1.lastIndexOf("}"));
            String args2 = s2.substring(s2.indexOf("(") + 1, s2.lastIndexOf(")"));
            if (args1.equals(args2)) {
                r = new Replacement(args1, args2, ReplacementType.ARRAY_INITIALIZER_REPLACED_WITH_METHOD_INVOCATION_ARGUMENTS);
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
        }
        if (variableDeclarationWithArrayInitializer2 != null
                && invocationCoveringTheEntireStatement1 != null
                && variableDeclarations1.isEmpty() &&
                !ReplacementFinder.containsMethodSignatureOfAnonymousClass(statement1.getText())
                && !ReplacementFinder.containsMethodSignatureOfAnonymousClass(statement2.getText())) {
            String args1 = s1.substring(s1.indexOf("(") + 1, s1.lastIndexOf(")"));
            String args2 = s2.substring(s2.indexOf("{") + 1, s2.lastIndexOf("}"));
            if (args1.equals(args2)) {
                r = new Replacement(args1, args2, ReplacementType.ARRAY_INITIALIZER_REPLACED_WITH_METHOD_INVOCATION_ARGUMENTS);
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
        }
        return false;
    }

    public boolean ternaryExpressionHeuristics(OperationInvocation invocationCoveringTheEntireStatement1
            , OperationInvocation invocationCoveringTheEntireStatement2
            , ObjectCreation creationCoveringTheEntireStatement1
            , ObjectCreation creationCoveringTheEntireStatement2
            , Set<String> creations1, Set<String> creations2
            , Set<String> creationIntersection
            , Map<String, List<? extends Invocation>> creationMap1
            , Map<String, List<? extends Invocation>> creationMap2
            , Set<String> methodInvocationIntersection) {

        List<TernaryOperatorExpression> ternaryOperatorExpressions1 = statement1.getTernaryOperatorExpressions();
        List<TernaryOperatorExpression> ternaryOperatorExpressions2 = statement2.getTernaryOperatorExpressions();
        Replacement r;

        if (ternaryOperatorExpressions1.isEmpty() && ternaryOperatorExpressions2.size() == 1) {
            TernaryOperatorExpression ternary = ternaryOperatorExpressions2.get(0);
            for (String creation : creationIntersection) {
                if ((r = ternary.makeReplacementWithTernaryOnTheRight(creation)) != null) {
                    replacementInfo.addReplacement(r);
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }
            for (String methodInvocation : methodInvocationIntersection) {
                if ((r = ternary.makeReplacementWithTernaryOnTheRight(methodInvocation)) != null) {
                    replacementInfo.addReplacement(r);
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }
            if (invocationCoveringTheEntireStatement1 != null && (r = ternary.makeReplacementWithTernaryOnTheRight(invocationCoveringTheEntireStatement1.actualString())) != null) {
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
            if (creationCoveringTheEntireStatement1 != null && (r = ternary.makeReplacementWithTernaryOnTheRight(creationCoveringTheEntireStatement1.actualString())) != null) {
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
            for (String creation2 : creations2) {
                if ((r = ternary.makeReplacementWithTernaryOnTheRight(creation2)) != null) {
                    for (Invocation c2 : creationMap2.get(creation2)) {
                        for (String creation1 : creations1) {
                            for (Invocation c1 : creationMap1.get(creation1)) {
                                if (
                                    /*((ObjectCreation) c1).getTypeName().compatibleTypes(((ObjectCreation) c2).getType()) &&*/ c1.equalArguments(c2)) {
                                    replacementInfo.addReplacement(r);
                                    // return replacementInfo.getReplacements();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (ternaryOperatorExpressions1.size() == 1 && ternaryOperatorExpressions2.isEmpty()) {
            TernaryOperatorExpression ternary = ternaryOperatorExpressions1.get(0);
            for (String creation : creationIntersection) {
                if ((r = ternary.makeReplacementWithTernaryOnTheLeft(creation)) != null) {
                    replacementInfo.addReplacement(r);
                    // return replacementInfo.getReplacements();
                    return true;
                }
            }
            for (String methodInvocation : methodInvocationIntersection) {
                if ((r = ternary.makeReplacementWithTernaryOnTheLeft(methodInvocation)) != null) {
                    replacementInfo.addReplacement(r);
                    //return replacementInfo.getReplacements();
                    return true;
                }
            }
            if (invocationCoveringTheEntireStatement2 != null && (r = ternary.makeReplacementWithTernaryOnTheLeft(invocationCoveringTheEntireStatement2.actualString())) != null) {
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
            if (creationCoveringTheEntireStatement2 != null && (r = ternary.makeReplacementWithTernaryOnTheLeft(creationCoveringTheEntireStatement2.actualString())) != null) {
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
            for (String creation1 : creations1) {
                if ((r = ternary.makeReplacementWithTernaryOnTheLeft(creation1)) != null) {
                    for (Invocation c1 : creationMap1.get(creation1)) {
                        for (String creation2 : creations2) {
                            for (Invocation c2 : creationMap2.get(creation2)) {
                                if (/*((ObjectCreation) c1).getType().compatibleTypes(((ObjectCreation) c2).getType())
                                        &&*/ c1.equalArguments(c2)) {
                                    replacementInfo.addReplacement(r);
                                    //return replacementInfo.getReplacements();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean fieldAssignentReplacedWithSetter(OperationInvocation
                                                            invocationCoveringTheEntireStatement2, Set<String> variables1) {
        Replacement r;
        if (invocationCoveringTheEntireStatement2 != null
                && statement2.getText().equals(invocationCoveringTheEntireStatement2.actualString() + JsConfig.STATEMENT_TERMINATOR_CHAR)
                &&
                invocationCoveringTheEntireStatement2.getArguments().size() == 1
                && statement1.getText().endsWith("=" + invocationCoveringTheEntireStatement2.getArguments().get(0) + JsConfig.STATEMENT_TERMINATOR_CHAR) &&
                invocationCoveringTheEntireStatement2.expressionIsNullOrThis() && invocationCoveringTheEntireStatement2.getName().startsWith("set")) {
            String prefix1 = statement1.getText().substring(0, statement1.getText().lastIndexOf("="));
            if (variables1.contains(prefix1)) {
                String before = prefix1 + "=" + invocationCoveringTheEntireStatement2.getArguments().get(0);
                String after = invocationCoveringTheEntireStatement2.actualString();
                r = new Replacement(before, after, ReplacementType.FIELD_ASSIGNMENT_REPLACED_WITH_SETTER_METHOD_INVOCATION);
                replacementInfo.addReplacement(r);
                //return replacementInfo.getReplacements();
                return true;
            }
        }
        return false;
    }
}