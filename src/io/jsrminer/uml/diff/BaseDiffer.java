package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.RenameVariableRefactoring;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.diff.detection.ConsistentReplacementDetector;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.InvocationCoverage;
import io.jsrminer.uml.mapping.replacement.MethodInvocationReplacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import io.rminerx.core.api.IContainer;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public abstract class BaseDiffer<T extends IContainer> {
    public static final double MAX_OPERATION_NAME_DISTANCE = 0.4;

    protected void updateMapperSet(TreeSet<FunctionBodyMapper> mapperSet, FunctionDeclaration removedOperation
            , FunctionDeclaration addedOperation, int differenceInPosition, ContainerDiff<T> sourceDiff) {
        FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(removedOperation, addedOperation, sourceDiff);

        List<CodeFragmentMapping> totalMappings = new ArrayList<>(operationBodyMapper.getMappings());
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        if (mappings > 0) {
            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation, sourceDiff.container1, sourceDiff.container2);
            if (exactMappings(operationBodyMapper, sourceDiff)) {
                mapperSet.add(operationBodyMapper);
            } else if (mappedElementsMoreThanNonMappedT1AndT2(mappings, operationBodyMapper)
                    && absoluteDifferenceInPosition <= differenceInPosition
                    && compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition, sourceDiff.getContainer1(), sourceDiff.getContainer2())
                //&& removedOperation.testAnnotationCheck(addedOperation)
            ) {
                mapperSet.add(operationBodyMapper);
            } else if (mappedElementsMoreThanNonMappedT2(mappings, operationBodyMapper
                    , new ArrayList<>(sourceDiff.getAddedOperations())
            ) && absoluteDifferenceInPosition <= differenceInPosition
                    && isPartOfMethodExtracted(removedOperation, addedOperation, sourceDiff.getAddedOperations())
                //        && removedOperation.testAnnotationCheck(addedOperation)
            ) {
                mapperSet.add(operationBodyMapper);
            } else if (mappedElementsMoreThanNonMappedT1(mappings, operationBodyMapper
                    , new ArrayList<>(sourceDiff.getRemovedOperations()))
                    && absoluteDifferenceInPosition <= differenceInPosition
                    && isPartOfMethodInlined(removedOperation, addedOperation, sourceDiff.getRemovedOperations())
                //                && removedOperation.testAnnotationCheck(addedOperation)
            ) {
                mapperSet.add(operationBodyMapper);
            }
        } else {

            Set<MethodInvocationReplacement> consistentMethodInvocationRenames = findConsistentMethodInvocationRenames(sourceDiff);
            for (MethodInvocationReplacement replacement : consistentMethodInvocationRenames) {
                if (replacement.getInvokedOperationBefore().matchesOperation(removedOperation) &&
                        replacement.getInvokedOperationAfter().matchesOperation(addedOperation)) {
                    mapperSet.add(operationBodyMapper);
                    break;
                }
            }
        }
        if (totalMappings.size() > 0) {
            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation, sourceDiff.container1, sourceDiff.container2);
            if (singleUnmatchedStatementCallsAddedOperation(operationBodyMapper, sourceDiff) &&
                    absoluteDifferenceInPosition <= differenceInPosition &&
                    compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition, sourceDiff.getContainer1(), sourceDiff.getContainer2())) {
                mapperSet.add(operationBodyMapper);
            }
        }

        if (mapperSet.isEmpty() && operationBodyMapper.getMappedNestedFunctionDeclrations().size() > 0) {
            if (isNestedFunctionMappersMatched(operationBodyMapper)) {
                mapperSet.add(operationBodyMapper);
            }
        }
    }


    private boolean isNestedFunctionMappersMatched(FunctionBodyMapper bodyMapper) {
        boolean isMatched = false;
        List<FunctionBodyMapper> nestedBodyMappers = new ArrayList<>(bodyMapper.getMappedNestedFunctionDeclrations());
        int functionMappings = nestedBodyMappers.size();
        var nonMappedNestedFunctionsT1 = bodyMapper.getNonMappedNestedFunctionDeclrationsT1().size();
        var nonMappedNestedFunctionsT2 = bodyMapper.getNonMappedNestedFunctionDeclrationsT2().size();

        if (functionMappings > 0) {
            int nonMappedFunctionsAndLeavesT1 = nonMappedNestedFunctionsT1 + bodyMapper.getNonMappedLeavesT1().size();
            int nonMappedFunctionsAndLeavesT2 = nonMappedNestedFunctionsT2 + bodyMapper.getNonMappedLeavesT2().size();

            boolean mappedFunctionsAndLeavesGreaterThanNonMappedFunctionsAndleaves =
                    (bodyMapper.mappingsWithoutBlocks() + functionMappings) >= nonMappedFunctionsAndLeavesT1
                    || (bodyMapper.mappingsWithoutBlocks() + functionMappings) >= nonMappedFunctionsAndLeavesT2;

            if (mappedFunctionsAndLeavesGreaterThanNonMappedFunctionsAndleaves) {
                boolean mappedNestedFunctionsMoreThanNonMappedT1AndT2 =
                        (functionMappings > nonMappedNestedFunctionsT1 && functionMappings > nonMappedNestedFunctionsT2) ||
                                (nonMappedNestedFunctionsT1 == 0 && functionMappings > Math.floor(nonMappedNestedFunctionsT2 / 2.0)) ||
                                (functionMappings == 1 && nonMappedNestedFunctionsT1 + nonMappedNestedFunctionsT2 == 1 && bodyMapper
                                        .function1.getName().equals(bodyMapper.function2.getName()));

                isMatched = mappedNestedFunctionsMoreThanNonMappedT1AndT2;
            }
        }

        return isMatched;
    }

    protected int computeAbsoluteDifferenceInPositionWithinClass(FunctionDeclaration removedOperation
            , FunctionDeclaration addedOperation, IContainer container1, IContainer container2) {
        int index1 = container1.getFunctionDeclarations().indexOf(removedOperation);
        int index2 = container2.getFunctionDeclarations().indexOf(addedOperation);
        return Math.abs(index1 - index2);
    }

    protected FunctionBodyMapper findBestMapper(TreeSet<FunctionBodyMapper> mapperSet) {
        List<FunctionBodyMapper> mapperList = new ArrayList<>(mapperSet);
        FunctionBodyMapper bestMapper = mapperSet.first();
        FunctionDeclaration bestMapperOperation1 = bestMapper.function1;
        FunctionDeclaration bestMapperOperation2 = bestMapper.function2;

        // TDOO return Param

//        if (bestMapperOperation1.equalReturnParameter(bestMapperOperation2) &&
//                bestMapperOperation1.name.equals(bestMapperOperation2.name) &&
//                bestMapperOperation1.commonParameterTypes(bestMapperOperation2).size() > 0) {
//            return bestMapper;
//        }
//
//        boolean identicalBodyWithOperation1OfTheBestMapper = identicalBodyWithAnotherAddedMethod(bestMapper);
//        boolean identicalBodyWithOperation2OfTheBestMapper = identicalBodyWithAnotherRemovedMethod(bestMapper);
//        for (int i = 1; i < mapperList.size(); i++) {
//            FunctionBodyMapper mapper = mapperList.get(i);
//            FunctionDeclaration operation2 = mapper.function2;
//            List<OperationInvocation> operationInvocations2 = operation2.getBody().getAllOperationInvocations();
//            boolean anotherMapperCallsOperation2OfTheBestMapper = false;
//            for (OperationInvocation invocation : operationInvocations2) {
//                if (invocation.matchesOperation(bestMapper.getOperation2(), operation2.variableTypeMap(), modelDiff) && !invocation.matchesOperation(bestMapper.getOperation1(), operation2.variableTypeMap(), modelDiff) &&
//                        !operationContainsMethodInvocationWithTheSameNameAndCommonArguments(invocation, removedOperations)) {
//                    anotherMapperCallsOperation2OfTheBestMapper = true;
//                    break;
//                }
//            }
//            FunctionDeclaration operation1 = mapper.function1;
//            List<OperationInvocation> operationInvocations1 = operation1.getBody().getAllOperationInvocations();
//            boolean anotherMapperCallsOperation1OfTheBestMapper = false;
//            for (OperationInvocation invocation : operationInvocations1) {
//                if (invocation.matchesOperation(bestMapper.function1/*, operation1.variableTypeMap(), modelDiff*/)
//                        && !invocation.matchesOperation(bestMapper.function2/*, operation1.variableTypeMap(), modelDiff*/) &&
//                        !operationContainsMethodInvocationWithTheSameNameAndCommonArguments(invocation, addedOperations)) {
//                    anotherMapperCallsOperation1OfTheBestMapper = true;
//                    break;
//                }
//            }
//            boolean nextMapperMatchesConsistentRename = matchesConsistentMethodInvocationRename(mapper, consistentMethodInvocationRenames);
//            boolean bestMapperMismatchesConsistentRename = mismatchesConsistentMethodInvocationRename(bestMapper, consistentMethodInvocationRenames);
//            if (bestMapperMismatchesConsistentRename && nextMapperMatchesConsistentRename) {
//                bestMapper = mapper;
//                break;
//            }
//            if (anotherMapperCallsOperation2OfTheBestMapper || anotherMapperCallsOperation1OfTheBestMapper) {
//                bestMapper = mapper;
//                break;
//            }
//            if (identicalBodyWithOperation2OfTheBestMapper || identicalBodyWithOperation1OfTheBestMapper) {
//                bestMapper = mapper;
//                break;
//            }
//        }
//        if (mismatchesConsistentMethodInvocationRename(bestMapper, consistentMethodInvocationRenames)) {
//            return null;
//        }
        return bestMapper;
    }

    protected boolean exactMappings(FunctionBodyMapper operationBodyMapper, ContainerDiff<T> containerDiff) {
        if (allMappingsAreExactMatches(operationBodyMapper)) {
            if (operationBodyMapper.nonMappedElementsT1() == 0 && operationBodyMapper.nonMappedElementsT2() == 0)
                return true;
            else if (operationBodyMapper.nonMappedElementsT1() > 0
                    && operationBodyMapper.getNonMappedInnerNodesT1().size() == 0
                    && operationBodyMapper.nonMappedElementsT2() == 0) {
                int countableStatements = 0;
                int parameterizedVariableDeclarationStatements = 0;
                FunctionDeclaration addedOperation = operationBodyMapper.function2;
                List<String> nonMappedLeavesT1 = new ArrayList<>();
                for (SingleStatement statement : operationBodyMapper.getNonMappedLeavesT1()) {
                    if (statement.countableStatement()) {
                        nonMappedLeavesT1.add(statement.getText());
                        for (String parameterName : addedOperation.getParameterNameList()) {
                            if (statement.getVariableDeclaration(parameterName) != null) {
                                parameterizedVariableDeclarationStatements++;
                                break;
                            }
                        }
                        countableStatements++;
                    }
                }
                int nonMappedLeavesExactlyMatchedInTheBodyOfAddedOperation = 0;
                for (FunctionDeclaration operation : containerDiff.getAddedOperations()) {
                    if (!operation.equals(addedOperation) && operation.getBody() != null) {
                        for (SingleStatement statement : operation.getBody().blockStatement.getAllLeafStatementsIncludingNested()) {
                            if (nonMappedLeavesT1.contains(statement.getText())) {
                                nonMappedLeavesExactlyMatchedInTheBodyOfAddedOperation++;
                            }
                        }
                    }
                }
                return (countableStatements == parameterizedVariableDeclarationStatements || countableStatements == nonMappedLeavesExactlyMatchedInTheBodyOfAddedOperation + parameterizedVariableDeclarationStatements) && countableStatements > 0;
            } else if (operationBodyMapper.nonMappedElementsT1() == 0
                    && operationBodyMapper.nonMappedElementsT2() > 0 && operationBodyMapper.getNonMappedInnerNodesT2().size() == 0) {
                int countableStatements = 0;
                int parameterizedVariableDeclarationStatements = 0;
                FunctionDeclaration removedOperation = operationBodyMapper.function1;
                for (SingleStatement statement : operationBodyMapper.getNonMappedLeavesT2()) {
                    if (statement.countableStatement()) {
                        for (String parameterName : removedOperation.getParameterNameList()) {
                            if (statement.getVariableDeclaration(parameterName) != null) {
                                parameterizedVariableDeclarationStatements++;
                                break;
                            }
                        }
                        countableStatements++;
                    }
                }
                return countableStatements == parameterizedVariableDeclarationStatements && countableStatements > 0;
            } else if ((operationBodyMapper.nonMappedElementsT1() == 1 || operationBodyMapper.nonMappedElementsT2() == 1) &&
                    operationBodyMapper.getNonMappedInnerNodesT1().size() == 0 && operationBodyMapper.getNonMappedInnerNodesT2().size() == 0) {
                SingleStatement statementUsingParameterAsInvoker1 = null;
                FunctionDeclaration removedOperation = operationBodyMapper.function1;
                for (SingleStatement statement : operationBodyMapper.getNonMappedLeavesT1()) {
                    if (statement.countableStatement()) {
                        for (String parameterName : removedOperation.getParameterNameList()) {
                            OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement);
                            if (invocation != null && invocation.getExpressionText() != null && invocation.getExpressionText().equals(parameterName)) {
                                statementUsingParameterAsInvoker1 = statement;
                                break;
                            }
                        }
                    }
                }
                SingleStatement statementUsingParameterAsInvoker2 = null;
                FunctionDeclaration addedOperation = operationBodyMapper.function2;
                for (SingleStatement statement : operationBodyMapper.getNonMappedLeavesT2()) {
                    if (statement.countableStatement()) {
                        for (String parameterName : addedOperation.getParameterNameList()) {
                            OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement);
                            if (invocation != null && invocation.getExpressionText() != null && invocation.getExpressionText().equals(parameterName)) {
                                statementUsingParameterAsInvoker2 = statement;
                                break;
                            }
                        }
                    }
                }
                if (statementUsingParameterAsInvoker1 != null && statementUsingParameterAsInvoker2 != null) {
                    for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
                        if (mapping.fragment1 instanceof BlockStatement && mapping.fragment2 instanceof BlockStatement) {
                            BlockStatement parent1 = (BlockStatement) mapping.fragment1;
                            BlockStatement parent2 = (BlockStatement) mapping.fragment2;
                            if (parent1.getAllLeafStatementsIncludingNested().contains(statementUsingParameterAsInvoker1)
                                    && parent2.getAllLeafStatementsIncludingNested().contains(statementUsingParameterAsInvoker2)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean allMappingsAreExactMatches(FunctionBodyMapper operationBodyMapper) {
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        int tryMappingsCount = 0;
        int mappingsWithTypeReplacement = 0;
        for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {

            // TODO recheck
            if (mapping.fragment1.getText().equals("try") && mapping.fragment2.getText().equals("try")) {
                tryMappingsCount++;
            }
            if (mapping.containsReplacement(ReplacementType.TYPE)) {
                mappingsWithTypeReplacement++;
            }
        }
        if (mappings == operationBodyMapper.getExactMatches().size() + tryMappingsCount) {
            return true;
        }
        if (mappings == operationBodyMapper.getExactMatches().size() + tryMappingsCount + mappingsWithTypeReplacement && mappings > mappingsWithTypeReplacement) {
            return true;
        }
        return false;
    }

    private boolean mappedElementsMoreThanNonMappedT1AndT2(int mappings, FunctionBodyMapper operationBodyMapper) {
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1();
        int nonMappedElementsT2 = operationBodyMapper.nonMappedElementsT2();
        return (mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) ||
                (nonMappedElementsT1 == 0 && mappings > Math.floor(nonMappedElementsT2 / 2.0)) ||
                (mappings == 1 && nonMappedElementsT1 + nonMappedElementsT2 == 1 && operationBodyMapper
                        .function1.getName().equals(operationBodyMapper.function2.getName()));
    }

    private boolean mappedElementsMoreThanNonMappedT2(int mappings, FunctionBodyMapper
            operationBodyMapper, List<FunctionDeclaration> addedOperations) {
        int nonMappedElementsT2 = operationBodyMapper.nonMappedElementsT2();
        int nonMappedElementsT2CallingAddedOperation = operationBodyMapper.nonMappedElementsT2CallingAddedOperation(addedOperations);
        int nonMappedElementsT2WithoutThoseCallingAddedOperation = nonMappedElementsT2 - nonMappedElementsT2CallingAddedOperation;
        return mappings > nonMappedElementsT2 || (mappings >= nonMappedElementsT2WithoutThoseCallingAddedOperation &&
                nonMappedElementsT2CallingAddedOperation >= nonMappedElementsT2WithoutThoseCallingAddedOperation);
    }

    private boolean mappedElementsMoreThanNonMappedT1(int mappings, FunctionBodyMapper
            operationBodyMapper, List<FunctionDeclaration> removedOperations) {
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1();
        int nonMappedElementsT1CallingRemovedOperation = operationBodyMapper.nonMappedElementsT1CallingRemovedOperation(removedOperations);
        int nonMappedElementsT1WithoutThoseCallingRemovedOperation = nonMappedElementsT1 - nonMappedElementsT1CallingRemovedOperation;
        return mappings > nonMappedElementsT1 || (mappings >= nonMappedElementsT1WithoutThoseCallingRemovedOperation &&
                nonMappedElementsT1CallingRemovedOperation >= nonMappedElementsT1WithoutThoseCallingRemovedOperation);
    }

//    public boolean testAnnotationCheck(FunctionDeclaration operation) {
//        if(this.hasTestAnnotation() && !operation.hasTestAnnotation())
//            return false;
//        if(!this.hasTestAnnotation() && operation.hasTestAnnotation())
//            return false;
//        return true;
//    }

    private boolean singleUnmatchedStatementCallsAddedOperation(FunctionBodyMapper
                                                                        operationBodyMapper, ContainerDiff<T> sourceDiff) {
        Set<SingleStatement> nonMappedLeavesT1 = operationBodyMapper.getNonMappedLeavesT1();
        Set<SingleStatement> nonMappedLeavesT2 = operationBodyMapper.getNonMappedLeavesT2();
        if (nonMappedLeavesT1.size() == 1 && nonMappedLeavesT2.size() == 1) {
            SingleStatement statementT2 = nonMappedLeavesT2.iterator().next();
            OperationInvocation invocationT2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statementT2);
            if (invocationT2 != null) {
                for (FunctionDeclaration addedOperation : sourceDiff.getAddedOperations()) {
                    if (invocationT2.matchesOperation(addedOperation/*, operationBodyMapper.function2, variableTypeMap(), modelDiff*/)) {
                        SingleStatement statementT1 = nonMappedLeavesT1.iterator().next();
                        OperationInvocation invocationT1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statementT1);
                        if (invocationT1 != null && addedOperation.getBody().getAllOperationInvocations().contains(invocationT1)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean compatibleSignatures(FunctionDeclaration removedOperation
            , FunctionDeclaration addedOperation, int absoluteDifferenceInPosition, IContainer container1, IContainer container2) {
        boolean isCompatibleParameters = FunctionUtil.compatibleSignature(removedOperation, addedOperation);
        return isCompatibleParameters ||
                ((absoluteDifferenceInPosition == 0
                        || operationsBeforeAndAfterMatch(removedOperation, addedOperation, container1, container2))
                        && (addedOperation.getParameters().size() == removedOperation.getParameters().size())
                        || normalizedNameDistance(removedOperation, addedOperation) <= MAX_OPERATION_NAME_DISTANCE
                );
    }


    public double normalizedNameDistance(FunctionDeclaration operation1, FunctionDeclaration operation2) {
        String s1 = operation1.getName().toLowerCase();
        String s2 = operation2.getName().toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    private boolean operationsBeforeAndAfterMatch(FunctionDeclaration removedOperation, FunctionDeclaration
            addedOperation, IContainer container1, IContainer container2) {
        FunctionDeclaration operationBefore1 = null;
        FunctionDeclaration operationAfter1 = null;

        FunctionDeclaration[] originalClassOperations = container1.getFunctionDeclarations().toArray(FunctionDeclaration[]::new);


        int removedOperationIndex = ArrayUtils.indexOf(container1.getFunctionDeclarations().toArray(), removedOperation);

        if (removedOperationIndex > 0) {
            operationBefore1 = originalClassOperations[removedOperationIndex - 1];
        }

        if (removedOperationIndex < originalClassOperations.length - 1) {
            operationAfter1 = originalClassOperations[removedOperationIndex + 1];
        }

        FunctionDeclaration[] nextClassOperations = container2.getFunctionDeclarations().toArray(FunctionDeclaration[]::new);
        int addedOperationIndex = ArrayUtils.indexOf(nextClassOperations, addedOperation);

        FunctionDeclaration operationBefore2 = null;
        FunctionDeclaration operationAfter2 = null;
        if (addedOperationIndex > 0) {
            operationBefore2 = nextClassOperations[addedOperationIndex - 1];
        }
        if (addedOperationIndex < nextClassOperations.length - 1) {
            operationAfter2 = nextClassOperations[addedOperationIndex + 1];
        }
//
//        for (int i = 0; i < originalClassOperations.length; i++) {
//            FunctionDeclaration current = originalClassOperations[i];
//
//            // TODO check equals
//            if (current.equals(removedOperation)) {
//                if (i > 0) {
//
//                }
//                if (i < originalClassOperations.length - 1) {
//                    operationAfter1 = originalClassOperations[i + 1];
//                }
//            }
//        }

//        FunctionDeclaration operationBefore2 = null;
//        FunctionDeclaration operationAfter2 = null;
//        FunctionDeclaration[] nextClassOperations = source2.getFunctionDeclarations();
//
//        for (int i = 0; i < nextClassOperations.length; i++) {
//            FunctionDeclaration current = nextClassOperations[i];
//            if (current.equals(addedOperation)) {
//                if (i > 0) {
//                    operationBefore2 = nextClassOperations[i - 1];
//                }
//                if (i < nextClassOperations.length - 1) {
//                    operationAfter2 = nextClassOperations[i + 1];
//                }
//            }
//        }

        boolean operationsBeforeMatch = false;
        if (operationBefore1 != null && operationBefore2 != null) {
            operationsBeforeMatch = operationBefore1.getName().equals(operationBefore2.getName()) &&
                    operationBefore1.getParameters().size() == operationBefore2.getParameters().size();
//              operationBefore1.equalParameterTypes(operationBefore2)
//                    && operationBefore1.getName().equals(operationBefore2.getName());
        }

        boolean operationsAfterMatch = false;
        if (operationAfter1 != null && operationAfter2 != null) {

            operationsAfterMatch = operationAfter1.getName().equals(operationAfter2.getName()) &&
                    operationAfter1.getParameters().size() == operationAfter2.getParameters().size();
            //operationsAfterMatch = operationAfter1.equalParameterTypes(operationAfter2) && operationAfter1.getName().equals(operationAfter2.getName());
        }

        return operationsBeforeMatch || operationsAfterMatch;
    }

    protected Set<MethodInvocationReplacement> findConsistentMethodInvocationRenames(ContainerDiff<T> sourceDiff) {
        Set<MethodInvocationReplacement> allConsistentMethodInvocationRenames = new LinkedHashSet<>();
        Set<MethodInvocationReplacement> allInconsistentMethodInvocationRenames = new LinkedHashSet<>();
        for (FunctionBodyMapper bodyMapper : sourceDiff.getOperationBodyMapperList()) {
            Set<MethodInvocationReplacement> methodInvocationRenames = bodyMapper.getMethodInvocationRenameReplacements();
            ConsistentReplacementDetector.updateRenames(allConsistentMethodInvocationRenames, allInconsistentMethodInvocationRenames,
                    methodInvocationRenames);
        }
        allConsistentMethodInvocationRenames.removeAll(allInconsistentMethodInvocationRenames);
        return allConsistentMethodInvocationRenames;
    }

    private boolean isPartOfMethodExtracted(FunctionDeclaration removedOperation, FunctionDeclaration
            addedOperation
            , List<FunctionDeclaration> addedOperations) {
        List<OperationInvocation> removedOperationInvocations = removedOperation.getBody().getAllOperationInvocations();
        List<OperationInvocation> addedOperationInvocations = addedOperation.getBody().getAllOperationInvocations();
        Set<OperationInvocation> intersection = new LinkedHashSet<>(removedOperationInvocations);
        intersection.retainAll(addedOperationInvocations);
        int numberOfInvocationsMissingFromRemovedOperation
                = new LinkedHashSet<>(removedOperationInvocations).size() - intersection.size();

        Set<OperationInvocation> operationInvocationsInMethodsCalledByAddedOperation
                = new LinkedHashSet<>();

        for (OperationInvocation addedOperationInvocation : addedOperationInvocations) {
            if (!intersection.contains(addedOperationInvocation)) {
                for (FunctionDeclaration operation : addedOperations) {
                    if (!operation.equals(addedOperation) && operation.getBody() != null) {
                        if (addedOperationInvocation.matchesOperation(operation/*, addedOperation.variableTypeMap(), modelDiff*/)) {
                            //addedOperation calls another added method
                            operationInvocationsInMethodsCalledByAddedOperation.addAll(operation.getBody().getAllOperationInvocations());
                        }
                    }
                }
            }
        }
        Set<OperationInvocation> newIntersection = new LinkedHashSet<>(removedOperationInvocations);
        newIntersection.retainAll(operationInvocationsInMethodsCalledByAddedOperation);

        Set<OperationInvocation> removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted = new LinkedHashSet<>(removedOperationInvocations);
        removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.removeAll(intersection);
        removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.removeAll(newIntersection);
        removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.removeIf(invocation -> invocation.getName().startsWith("get"));
        int numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations = newIntersection.size();
        int numberOfInvocationsMissingFromRemovedOperationWithoutThoseFoundInOtherAddedOperations = numberOfInvocationsMissingFromRemovedOperation - numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations;
        return numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations > numberOfInvocationsMissingFromRemovedOperationWithoutThoseFoundInOtherAddedOperations ||
                numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations > removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.size();
    }

    private boolean isPartOfMethodInlined(FunctionDeclaration removedOperation, FunctionDeclaration
            addedOperation, List<FunctionDeclaration> removedOperations) {
        List<OperationInvocation> removedOperationInvocations = removedOperation.getBody().getAllOperationInvocations();
        List<OperationInvocation> addedOperationInvocations = addedOperation.getBody().getAllOperationInvocations();
        Set<OperationInvocation> intersection = new LinkedHashSet<>(removedOperationInvocations);
        intersection.retainAll(addedOperationInvocations);
        int numberOfInvocationsMissingFromAddedOperation = new LinkedHashSet<>(addedOperationInvocations).size() - intersection.size();

        Set<OperationInvocation> operationInvocationsInMethodsCalledByRemovedOperation = new LinkedHashSet<>();
        for (OperationInvocation removedOperationInvocation : removedOperationInvocations) {
            if (!intersection.contains(removedOperationInvocation)) {
                for (FunctionDeclaration operation : removedOperations) {
                    if (!operation.equals(removedOperation) && operation.getBody() != null) {
                        if (removedOperationInvocation.matchesOperation(operation/*, removedOperation.variableTypeMap(), modelDiff*/)) {
                            //removedOperation calls another removed method
                            operationInvocationsInMethodsCalledByRemovedOperation.addAll(operation.getBody().getAllOperationInvocations());
                        }
                    }
                }
            }
        }
        Set<OperationInvocation> newIntersection = new LinkedHashSet<OperationInvocation>(addedOperationInvocations);
        newIntersection.retainAll(operationInvocationsInMethodsCalledByRemovedOperation);

        int numberOfInvocationsCalledByAddedOperationFoundInOtherRemovedOperations = newIntersection.size();
        int numberOfInvocationsMissingFromAddedOperationWithoutThoseFoundInOtherRemovedOperations = numberOfInvocationsMissingFromAddedOperation - numberOfInvocationsCalledByAddedOperationFoundInOtherRemovedOperations;
        return numberOfInvocationsCalledByAddedOperationFoundInOtherRemovedOperations > numberOfInvocationsMissingFromAddedOperationWithoutThoseFoundInOtherRemovedOperations;
    }

    protected void checkForInconsistentVariableRenames(FunctionBodyMapper mapper, ContainerDiff<T> sourceDiff) {
        if (mapper.getChildMappers().size() > 1) {
            Set<IRefactoring> refactoringsToBeRemoved = new LinkedHashSet<>();
            for (IRefactoring r : sourceDiff.getRefactoringsBeforePostProcessing()) {
                if (r instanceof RenameVariableRefactoring) {
                    RenameVariableRefactoring rename = (RenameVariableRefactoring) r;
                    Set<CodeFragmentMapping> references = rename.getVariableReferences();
                    for (CodeFragmentMapping reference : references) {
                        if (reference.getFragment1().getVariableDeclarations().size() > 0 && !reference.isExact()) {
                            Set<CodeFragmentMapping> allMappingsForReference = new LinkedHashSet<>();
                            for (FunctionBodyMapper childMapper : mapper.getChildMappers()) {
                                for (CodeFragmentMapping mapping : childMapper.getMappings()) {
                                    if (mapping.getFragment1().equals(reference.getFragment1())) {
                                        allMappingsForReference.add(mapping);
                                        break;
                                    }
                                }
                            }
                            if (allMappingsForReference.size() > 1) {
                                for (CodeFragmentMapping mapping : allMappingsForReference) {
                                    if (!mapping.equals(reference) && mapping.isExact()) {
                                        refactoringsToBeRemoved.add(rename);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            sourceDiff.getRefactoringsBeforePostProcessing().removeAll(refactoringsToBeRemoved);
        }
    }
}
