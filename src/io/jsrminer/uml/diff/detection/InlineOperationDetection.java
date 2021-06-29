package io.jsrminer.uml.diff.detection;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.*;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.InvocationCoverage;
import io.rminerx.core.api.IContainer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InlineOperationDetection {
    private FunctionBodyMapper mapper;
    private List<FunctionDeclaration> removedOperations;
    private ContainerDiff<? extends IContainer> containerDiff;
    //private UMLModelDiff modelDiff;   // only needed for matching invocation using types
    private List<OperationInvocation> operationInvocations;
    private Map<CallTreeNode, CallTree> callTreeMap = new LinkedHashMap<>();

    public InlineOperationDetection(FunctionBodyMapper mapper
            , List<FunctionDeclaration> removedOperations
            , ContainerDiff<? extends IContainer> containerDiff/*, UMLModelDiff modelDiff*/) {
        this.mapper = mapper;
        this.removedOperations = removedOperations;
        this.containerDiff = containerDiff;
        //this.modelDiff = modelDiff;
        this.operationInvocations = getInvocationsInTargetOperationBeforeInline(mapper);
    }

    public List<InlineOperationRefactoring> check(FunctionDeclaration removedOperation) throws RefactoringMinerTimedOutException {
        List<InlineOperationRefactoring> refactorings = new ArrayList<>();

        if (!mapper.getNonMappedLeavesT2().isEmpty()
                || !mapper.getNonMappedInnerNodesT2().isEmpty() ||
                !mapper.getReplacementsInvolvingMethodInvocation().isEmpty()) {

            List<OperationInvocation> removedOperationInvocations
                    = matchingInvocations(removedOperation, operationInvocations/*, mapper.function1.variableTypeMap()*/);

            if (removedOperationInvocations.size() > 0
                    && !invocationMatchesWithAddedOperation(removedOperationInvocations.get(0)/*, mapper.function2.variableTypeMap()*/, mapper.function2.getBody().getAllOperationInvocations())) {
                OperationInvocation removedOperationInvocation = removedOperationInvocations.get(0);
                //processRemovedInvockedOperation(removedOperation, removedOperationInvocation, this.mapper, removedOperationInvocations, refactorings);


                CallTreeNode root = new CallTreeNode(mapper.function1, removedOperation, removedOperationInvocation);
                CallTree callTree = null;
                if (callTreeMap.containsKey(root)) {
                    callTree = callTreeMap.get(root);
                } else {
                    callTree = new CallTree(root);
                    generateCallTree(removedOperation, root, callTree);
                    callTreeMap.put(root, callTree);
                }
                FunctionBodyMapper operationBodyMapper = createMapperForInlinedMethod(mapper, removedOperation, removedOperationInvocation);
                List<CodeFragmentMapping> additionalExactMatches = new ArrayList<>();
                List<CallTreeNode> nodesInBreadthFirstOrder = callTree.getNodesInBreadthFirstOrder();

                for (int i = 1; i < nodesInBreadthFirstOrder.size(); i++) {
                    CallTreeNode node = nodesInBreadthFirstOrder.get(i);

                    if (matchingInvocations(node.getInvokedOperation(), operationInvocations/*, mapper.function1.variableTypeMap()*/).size() == 0) {

                        FunctionBodyMapper nestedMapper = createMapperForInlinedMethod(mapper, node.getInvokedOperation(), node.getInvocation());
                        additionalExactMatches.addAll(nestedMapper.getExactMatches());
                        if (inlineMatchCondition(nestedMapper)) {
                            List<OperationInvocation> nestedMatchingInvocations = matchingInvocations(node.getInvokedOperation()
                                    , node.getOriginalOperation().getBody().getAllOperationInvocations());
                            InlineOperationRefactoring nestedRefactoring = new InlineOperationRefactoring(nestedMapper, mapper.function1, nestedMatchingInvocations);
                            refactorings.add(nestedRefactoring);
                            operationBodyMapper.addChildMapper(nestedMapper);
                        }
                    }
                }
                if (inlineMatchCondition(operationBodyMapper)) {
                    InlineOperationRefactoring inlineOperationRefactoring = new InlineOperationRefactoring(operationBodyMapper, mapper.function1, removedOperationInvocations);
                    refactorings.add(inlineOperationRefactoring);
                }
            }
        }
        return refactorings;
    }

    private void processRemovedInvockedOperation(FunctionDeclaration removedOperation, OperationInvocation removedOperationInvocation, FunctionBodyMapper mapper, List<OperationInvocation> removedOperationInvocations, List<InlineOperationRefactoring> refactorings) {
    }

    private List<OperationInvocation> matchingInvocations(FunctionDeclaration removedOperation, List<OperationInvocation> operationInvocations
            /*, Map<String, UMLType> variableTypeMap*/) {
        List<OperationInvocation> removedOperationInvocations = new ArrayList<>();
        for (OperationInvocation invocation : operationInvocations) {
            if (invocation.matchesOperation(removedOperation/*, variableTypeMap, modelDiff*/)) {
                removedOperationInvocations.add(invocation);
            }
        }
        return removedOperationInvocations;
    }

    private FunctionBodyMapper createMapperForInlinedMethod(FunctionBodyMapper mapper,
                                                            FunctionDeclaration removedOperation, OperationInvocation removedOperationInvocation) throws RefactoringMinerTimedOutException {
        List<String> arguments = removedOperationInvocation.getArguments();
        List<UMLParameter> parameters = new ArrayList<>(removedOperation.getParameters());//

        Map<String, String> parameterToArgumentMap = new LinkedHashMap<>();
        //special handling for methods with varargs parameter for which no argument is passed in the matching invocation
        int size = Math.min(arguments.size(), parameters.size());
        for (int i = 0; i < size; i++) {
            parameterToArgumentMap.put(parameters.get(i).name, arguments.get(i));
        }

        FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(removedOperation, mapper, parameterToArgumentMap, containerDiff);
        return operationBodyMapper;
    }

    private void generateCallTree(FunctionDeclaration operation, CallTreeNode parent, CallTree callTree) {
        List<OperationInvocation> invocations = operation.getBody().getAllOperationInvocations();
        for (FunctionDeclaration removedOperation : removedOperations) {
            for (OperationInvocation invocation : invocations) {
                if (invocation.matchesOperation(removedOperation)) {
                    if (!callTree.contains(removedOperation)) {
                        CallTreeNode node = new CallTreeNode(operation, removedOperation, invocation);
                        parent.addChild(node);
                        generateCallTree(removedOperation, node, callTree);
                    }
                }
            }
        }
    }

    private List<OperationInvocation> getInvocationsInTargetOperationBeforeInline(FunctionBodyMapper mapper) {
        List<OperationInvocation> operationInvocations = new ArrayList<>(mapper.function1.getBody().getAllOperationInvocations());
        for (SingleStatement statement : mapper.getNonMappedLeavesT1()) {
            ExtractOperationDetection.addStatementInvocations(operationInvocations, statement);
//            for (UMLAnonymousClass anonymousClass : classDiff.getRemovedAnonymousClasses()) {
//                if (statement.getLocationInfo().subsumes(anonymousClass.getLocationInfo())) {
//                    for (FunctionDeclaration anonymousOperation : anonymousClass.getOperations()) {
//                        for (OperationInvocation anonymousInvocation : anonymousOperation.getAllOperationInvocations()) {
//                            if (!ExtractOperationDetection.containsInvocation(operationInvocations, anonymousInvocation)) {
//                                operationInvocations.add(anonymousInvocation);
//                            }
//                        }
//                    }
//                }
//            }
        }
        return operationInvocations;
    }

    private boolean inlineMatchCondition(FunctionBodyMapper operationBodyMapper) {
        int delegateStatements = 0;
        for (SingleStatement statement : operationBodyMapper.getNonMappedLeavesT1()) {
            OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement);
            if (invocation != null && invocation.matchesOperation(operationBodyMapper.function1)) {
                delegateStatements++;
            }
        }
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1() - delegateStatements;
        List<CodeFragmentMapping> exactMatchList = operationBodyMapper.getExactMatches();
        int exactMatches = exactMatchList.size();
        return mappings > 0 && (mappings > nonMappedElementsT1 ||
                (exactMatches == 1 /*&& !exactMatchList.get(0).fragment1.throwsNewException()*/
                        && nonMappedElementsT1 - exactMatches < 10) ||
                (exactMatches > 1 && nonMappedElementsT1 - exactMatches < 20));
    }

    private boolean invocationMatchesWithAddedOperation(OperationInvocation removedOperationInvocation
            /*, Map<String, UMLType> variableTypeMap*/
            , List<OperationInvocation> operationInvocationsInNewMethod) {
        if (operationInvocationsInNewMethod.contains(removedOperationInvocation)) {
            for (FunctionDeclaration addedOperation : containerDiff.getAddedOperations()) {
                if (removedOperationInvocation.matchesOperation(addedOperation/*, variableTypeMap, modelDiff*/)) {
                    return true;
                }
            }
        }
        return false;
    }
}
