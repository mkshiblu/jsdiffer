package io.jsrminer.uml.diff.detection;

import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.CallTree;
import io.jsrminer.uml.diff.CallTreeNode;
import io.jsrminer.uml.diff.ContainerDiff;
import io.jsrminer.uml.mapping.Argumentizer;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.ReplacementType;

import java.util.*;

public class ExtractOperationDetection {
    private FunctionBodyMapper mapper;
    private List<FunctionDeclaration> addedOperations;
    private ContainerDiff classDiff;
    //private UMLModelDiff modelDiff;
    private List<OperationInvocation> operationInvocations;
    private Map<CallTreeNode, CallTree> callTreeMap = new LinkedHashMap<>();

    Argumentizer argumentizer;

    public ExtractOperationDetection(FunctionBodyMapper mapper, List<FunctionDeclaration> addedOperations, ContainerDiff classDiff/*, UMLModelDiff modelDiff*/) {
        this.mapper = mapper;
        this.addedOperations = addedOperations;
        this.classDiff = classDiff;
        //  this.modelDiff = modelDiff;
        this.operationInvocations = getInvocationsInSourceOperationAfterExtraction(mapper);
        this.argumentizer = mapper.argumentizer;
    }

    public List<ExtractOperationRefactoring> check(FunctionDeclaration addedOperation) {
        List<ExtractOperationRefactoring> refactorings = new ArrayList<>();

        if (!mapper.getNonMappedLeavesT1().isEmpty()
                || !mapper.getNonMappedInnerNodesT1().isEmpty() ||
                !mapper.getReplacementsInvolvingMethodInvocation().isEmpty()) {

            List<OperationInvocation> addedOperationInvocations = matchingInvocations(addedOperation, operationInvocations);
            if (addedOperationInvocations.size() > 0) {
                int otherAddedMethodsCalled = 0;
                for (FunctionDeclaration addedOperation2 : this.addedOperations) {
                    if (!addedOperation.equals(addedOperation2)) {
                        List<OperationInvocation> addedOperationInvocations2 = matchingInvocations(addedOperation2, operationInvocations/*, mapper.getOperation2().variableTypeMap()*/);
                        if (addedOperationInvocations2.size() > 0) {
                            otherAddedMethodsCalled++;
                        }
                    }
                }
                if (otherAddedMethodsCalled == 0) {
                    List<OperationInvocation> sortedInvocations = sortInvocationsBasedOnArgumentOccurrences(addedOperationInvocations);
                    for (OperationInvocation addedOperationInvocation : sortedInvocations) {
                        processAddedOperation(mapper, addedOperation, refactorings, addedOperationInvocations, addedOperationInvocation);
                        //processInvokedAddedOperation(addedOperationInvocations.get(0), addedOperation, this.mapper, addedOperationInvocations, refactorings);
                    }
                } else {
                    processAddedOperation(mapper, addedOperation, refactorings, addedOperationInvocations, addedOperationInvocations.get(0));
                    //processInvokedAddedOperation(addedOperationInvocations.get(0), addedOperation, this.mapper, addedOperationInvocations, refactorings);
                }
            }
        }
        return refactorings;
    }

    private void processInvokedAddedOperation(OperationInvocation operationInvocation
            , FunctionDeclaration invokedAddedOperation
            , FunctionBodyMapper mapper
            , List<OperationInvocation> addedOperationInvocations
            , List<ExtractOperationRefactoring> refactorings) {
        FunctionBodyMapper operationBodyMapper = createMapperForExtractedMethod(mapper
                , mapper.function1, invokedAddedOperation, operationInvocation);

        List<CodeFragmentMapping> additionalExactMatches = new ArrayList<>();
        FunctionDeclaration delegateMethod = findDelegateMethod(mapper.function1, invokedAddedOperation, operationInvocation);
        if (extractMatchCondition(operationBodyMapper, additionalExactMatches)) {
            if (delegateMethod == null) {
                refactorings.add(new ExtractOperationRefactoring(operationBodyMapper, mapper.function2, addedOperationInvocations));
            } else {
                refactorings.add(new ExtractOperationRefactoring(operationBodyMapper, invokedAddedOperation,
                        mapper.function1, mapper.function2, addedOperationInvocations));
            }
        }
    }

    private void processAddedOperation(FunctionBodyMapper mapper, FunctionDeclaration addedOperation,
                                       List<ExtractOperationRefactoring> refactorings,
                                       List<OperationInvocation> addedOperationInvocations, OperationInvocation addedOperationInvocation) {

        CallTreeNode root = new CallTreeNode(mapper.function1, addedOperation, addedOperationInvocation);
        CallTree callTree = null;
        if (callTreeMap.containsKey(root)) {
            callTree = callTreeMap.get(root);
        } else {
            callTree = new CallTree(root);
            generateCallTree(addedOperation, root, callTree);
            callTreeMap.put(root, callTree);
        }
        FunctionBodyMapper operationBodyMapper = createMapperForExtractedMethod(mapper
                , mapper.function1, addedOperation, addedOperationInvocation);

        if (operationBodyMapper != null) {
            List<CodeFragmentMapping> additionalExactMatches = new ArrayList<>();
            List<CallTreeNode> nodesInBreadthFirstOrder = callTree.getNodesInBreadthFirstOrder();

            for (int i = 1; i < nodesInBreadthFirstOrder.size(); i++) {
                CallTreeNode node = nodesInBreadthFirstOrder.get(i);
                if (matchingInvocations(node.getInvokedOperation(), this.operationInvocations).size() == 0) {
                    FunctionBodyMapper nestedMapper = createMapperForExtractedMethod(mapper, node.getOriginalOperation(), node.getInvokedOperation(), node.getInvocation());
                    if (nestedMapper != null) {
                        additionalExactMatches.addAll(nestedMapper.getExactMatches());
                        if (extractMatchCondition(nestedMapper, new ArrayList<>())
                                && extractMatchCondition(operationBodyMapper, additionalExactMatches)) {
                            List<OperationInvocation> nestedMatchingInvocations
                                    = matchingInvocations(node.getInvokedOperation()
                                    , node.getOriginalOperation().getBody().getAllOperationInvocations());
                            ExtractOperationRefactoring nestedRefactoring =
                                    new ExtractOperationRefactoring(nestedMapper, mapper.function2, nestedMatchingInvocations);
                            refactorings.add(nestedRefactoring);
                            operationBodyMapper.addChildMapper(nestedMapper);
                        }
                        //add back to mapper non-exact matches
                        for (CodeFragmentMapping mapping : nestedMapper.getMappings()) {
                            if (!mapping.isExact() || mapping.fragment1.getText().equals("{")) {
                                CodeFragment fragment1 = mapping.fragment1;
                                if (fragment1 instanceof SingleStatement) {
                                    if (!mapper.getNonMappedLeavesT1().contains(fragment1)) {
                                        mapper.getNonMappedLeavesT1().add((SingleStatement) fragment1);
                                    }
                                } else if (fragment1 instanceof BlockStatement) {
                                    if (!mapper.getNonMappedInnerNodesT1().contains(fragment1)) {
                                        mapper.getNonMappedInnerNodesT1().add((BlockStatement) fragment1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FunctionDeclaration delegateMethod = findDelegateMethod(mapper.function1, addedOperation, addedOperationInvocation);
            if (extractMatchCondition(operationBodyMapper, additionalExactMatches)) {
                if (delegateMethod == null) {
                    refactorings.add(new ExtractOperationRefactoring(operationBodyMapper, mapper.function2, addedOperationInvocations));
                } else {
                    refactorings.add(new ExtractOperationRefactoring(operationBodyMapper, addedOperation,
                            mapper.function1, mapper.function2, addedOperationInvocations));
                }
            }
        }
    }

    public static List<OperationInvocation> getInvocationsInSourceOperationAfterExtraction(FunctionBodyMapper mapper) {
        List<OperationInvocation> operationInvocations = mapper.function2.getBody() != null ?
                mapper.function2.getBody().getAllOperationInvocations() : new ArrayList<>();

        for (SingleStatement statement : mapper.getNonMappedLeavesT2()) {
            addStatementInvocations(operationInvocations, statement);
        }

        return operationInvocations;
    }

    public static void addStatementInvocations(List<OperationInvocation> operationInvocations, SingleStatement statement) {
        Map<String, List<OperationInvocation>> statementMethodInvocationMap = statement.getMethodInvocationMap();
        for (String key : statementMethodInvocationMap.keySet()) {
            for (OperationInvocation statementInvocation : statementMethodInvocationMap.get(key)) {
                if (!containsInvocation(operationInvocations, statementInvocation)) {
                    operationInvocations.add(statementInvocation);
                }
            }
        }
    }

    public static boolean containsInvocation(List<OperationInvocation> operationInvocations, OperationInvocation invocation) {
        for (OperationInvocation operationInvocation : operationInvocations) {
            if (operationInvocation.equalsSourceLocation(invocation)) {
                return true;
            }
        }
        return false;
    }

    private List<OperationInvocation> matchingInvocations(FunctionDeclaration operation,
                                                          List<OperationInvocation> operationInvocations) {
        List<OperationInvocation> addedOperationInvocations = new ArrayList<>();
        for (OperationInvocation invocation : operationInvocations) {
            if (invocation.matchesOperation(operation/*, modelDiff*/)) {
                addedOperationInvocations.add(invocation);
            }
        }
        return addedOperationInvocations;
    }

    private void generateCallTree(FunctionDeclaration operation, CallTreeNode parent, CallTree callTree) {
        List<OperationInvocation> invocations = operation.getBody().getAllOperationInvocations();
        for (FunctionDeclaration addedOperation : addedOperations) {
            for (OperationInvocation invocation : invocations) {
                if (invocation.matchesOperation(addedOperation)) {
                    if (!callTree.contains(addedOperation)) {
                        CallTreeNode node = new CallTreeNode(operation, addedOperation, invocation);
                        parent.addChild(node);
                        generateCallTree(addedOperation, node, callTree);
                    }
                }
            }
        }
    }

    /**
     * Main for detectiing extract mehtod
     *
     * @return
     */
    private FunctionBodyMapper createMapperForExtractedMethod(FunctionBodyMapper mapper
            , FunctionDeclaration originalOperation
            , FunctionDeclaration addedOperation, OperationInvocation addedOperationInvocation) {

        //Map<String, UMLParameter> originalMethodParameters = originalOperation.getParameters();
        //    Map<UMLParameter, UMLParameter> originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters
        //        = new LinkedHashMap<>();

        List<String> invocationArguments = addedOperationInvocation.getArguments();
        List<UMLParameter> addedOperationParameters = new ArrayList<>(addedOperation.getParameters());
        Map<String, String> parameterToArgumentMap = new LinkedHashMap<>();
        int size = Math.min(invocationArguments.size(), addedOperationParameters.size());
        for (int i = 0; i < size; i++) {
            String argumentName = invocationArguments.get(i);
            String parameterName = addedOperationParameters.get(i).name;

            parameterToArgumentMap.put(parameterName, argumentName);
//            for (UMLParameter originalMethodParameter : originalMethodParameters) {
//                if (originalMethodParameter.getName().equals(argumentName)) {
//                    originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters.put(originalMethodParameter, parameters.get(i));
//                }
//            }
        }

        boolean sameParameterCount = invocationArguments.size() == addedOperationParameters.size();
        if (sameParameterCount) {
            return new FunctionBodyMapper(mapper, addedOperation, classDiff, new LinkedHashMap<>(), parameterToArgumentMap);
        }
        return null;
    }

    private boolean extractMatchCondition(FunctionBodyMapper operationBodyMapper, List<CodeFragmentMapping> additionalExactMatches) {
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1();
        int nonMappedElementsT2 = operationBodyMapper.nonMappedElementsT2();
        List<CodeFragmentMapping> exactMatchList = new ArrayList<>(operationBodyMapper.getExactMatches());
        boolean exceptionHandlingExactMatch = false;
        // boolean throwsNewExceptionExactMatch = false;
        if (exactMatchList.size() == 1) {
            CodeFragmentMapping mapping = exactMatchList.get(0);
            if (mapping.fragment1 instanceof SingleStatement && mapping.fragment2 instanceof SingleStatement) {
                SingleStatement statement1 = (SingleStatement) mapping.fragment1;
                SingleStatement statement2 = (SingleStatement) mapping.fragment2;

                if (statement1.getParent().getText().startsWith("catch(") &&
                        statement2.getParent().getText().startsWith("catch(")) {
                    exceptionHandlingExactMatch = true;
                }
            }
            //   if (mapping.fragment1.throwsNewException() && mapping.fragment2.throwsNewException()) {
            //     throwsNewExceptionExactMatch = true;
            // }
        }
        exactMatchList.addAll(additionalExactMatches);
        int exactMatches = exactMatchList.size();
        return mappings > 0 && (mappings > nonMappedElementsT2 || (mappings > 1 && mappings >= nonMappedElementsT2) ||
                (exactMatches >= mappings && nonMappedElementsT1 == 0) ||
                (exactMatches == 1 /*&& !throwsNewExceptionExactMatch*/ && nonMappedElementsT2 - exactMatches <= 10) ||
                (!exceptionHandlingExactMatch && exactMatches > 1 && additionalExactMatches.size() < exactMatches && nonMappedElementsT2 - exactMatches < 20) ||
                (mappings == 1 && mappings > operationBodyMapper.nonMappedLeafElementsT2())) ||
                argumentExtractedWithDefaultReturnAdded(operationBodyMapper);
    }

    private boolean argumentExtractedWithDefaultReturnAdded(FunctionBodyMapper operationBodyMapper) {
        List<CodeFragmentMapping> totalMappings = new ArrayList<>(operationBodyMapper.getMappings());
        List<BlockStatement> nonMappedInnerNodesT2 = new ArrayList<>(operationBodyMapper.getNonMappedInnerNodesT2());
        ListIterator<BlockStatement> iterator = nonMappedInnerNodesT2.listIterator();

        while (iterator.hasNext()) {
            if (iterator.next().toString().equals("{")) {
                iterator.remove();
            }
        }
        Set<SingleStatement> nonMappedLeavesT2 = operationBodyMapper.getNonMappedLeavesT2();
        return totalMappings.size() == 1 && totalMappings.get(0)
                .containsReplacement(ReplacementType.ARGUMENT_REPLACED_WITH_RETURN_EXPRESSION) &&
                nonMappedInnerNodesT2.size() == 1 && nonMappedInnerNodesT2.get(0).toString().startsWith("if") &&
                nonMappedLeavesT2.size() == 1 && nonMappedLeavesT2.iterator().next().toString().startsWith("return ");
    }

    private FunctionDeclaration findDelegateMethod(FunctionDeclaration originalOperation, FunctionDeclaration addedOperation, OperationInvocation addedOperationInvocation) {
        OperationInvocation delegateMethodInvocation = FunctionUtil.isDelegate(addedOperation);
        if (FunctionUtil.isDelegate(originalOperation) == null && delegateMethodInvocation != null
                && !FunctionUtil.containsInvocation(originalOperation, addedOperationInvocation)) {
            for (FunctionDeclaration operation : addedOperations) {
                if (delegateMethodInvocation.matchesOperation(operation)) {
                    return operation;
                }
            }
        }
        return null;
    }


    private boolean parameterTypesMatch(Map<UMLParameter, UMLParameter> originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters) {
//        for (UMLParameter key : originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters.keySet()) {
//            UMLParameter value = originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters.get(key);
//            if (!key.getType().equals(value.getType()) && !key.getType().equalsWithSubType(value.getType()) &&
//                    !modelDiff.isSubclassOf(key.getType().getClassType(), value.getType().getClassType())) {
//                return false;
//            }
//        }
        return true;
    }

    private List<OperationInvocation> sortInvocationsBasedOnArgumentOccurrences(List<OperationInvocation> invocations) {
        if (invocations.size() > 1) {
            List<OperationInvocation> sorted = new ArrayList<OperationInvocation>();
            List<String> allVariables = new ArrayList<String>();
            for (BlockStatement composite : mapper.getNonMappedInnerNodesT1()) {
                allVariables.addAll(composite.getVariables());
            }
            for (SingleStatement leaf : mapper.getNonMappedLeavesT1()) {
                allVariables.addAll(leaf.getVariables());
            }
            int max = 0;
            for (OperationInvocation invocation : invocations) {
                List<String> arguments = invocation.getArguments();
                int occurrences = 0;
                for (String argument : arguments) {
                    occurrences += Collections.frequency(allVariables, argument);
                }
                if (occurrences > max) {
                    sorted.add(0, invocation);
                    max = occurrences;
                } else {
                    sorted.add(invocation);
                }
            }
            return sorted;
        } else {
            return invocations;
        }
    }
}
