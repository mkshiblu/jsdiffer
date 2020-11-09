package io.jsrminer.uml.diff.detection;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.uml.diff.CallTree;
import io.jsrminer.uml.diff.CallTreeNode;
import io.jsrminer.uml.diff.SourceFileModelDiff;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.jsrminer.uml.mapping.FunctionBodyMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InlineOperationDetection {
    private FunctionBodyMapper mapper;
    private List<FunctionDeclaration> removedOperations;
    private SourceFileModelDiff classDiff;
    private UMLModelDiff modelDiff;
    private List<OperationInvocation> operationInvocations;
    private Map<CallTreeNode, CallTree> callTreeMap = new LinkedHashMap<>();

    public InlineOperationDetection(FunctionBodyMapper mapper
            , List<FunctionDeclaration> removedOperations
            , SourceFileModelDiff classDiff, UMLModelDiff modelDiff) {
        this.mapper = mapper;
        this.removedOperations = removedOperations;
        this.classDiff = classDiff;
        this.modelDiff = modelDiff;
        this.operationInvocations = getInvocationsInTargetOperationBeforeInline(mapper);
    }

    public List<InlineOperationRefactoring> check(FunctionDeclaration removedOperation) throws RefactoringMinerTimedOutException {
        List<InlineOperationRefactoring> refactorings = new ArrayList<>();
        if (!mapper.getNonMappedLeavesT2().isEmpty() || !mapper.getNonMappedInnerNodesT2().isEmpty() ||
                !mapper.getReplacementsInvolvingMethodInvocation().isEmpty()) {

            List<OperationInvocation> removedOperationInvocations = matchingInvocations(removedOperation, operationInvocations, mapper.getOperation1().variableTypeMap());
            if (removedOperationInvocations.size() > 0 && !invocationMatchesWithAddedOperation(removedOperationInvocations.get(0), mapper.getOperation1().variableTypeMap(), mapper.getOperation2().getAllOperationInvocations())) {
                OperationInvocation removedOperationInvocation = removedOperationInvocations.get(0);
                CallTreeNode root = new CallTreeNode(mapper.getOperation1(), removedOperation, removedOperationInvocation);
                CallTree callTree = null;
                if (callTreeMap.containsKey(root)) {
                    callTree = callTreeMap.get(root);
                } else {
                    callTree = new CallTree(root);
                    generateCallTree(removedOperation, root, callTree);
                    callTreeMap.put(root, callTree);
                }
                UMLOperationBodyMapper operationBodyMapper = createMapperForInlinedMethod(mapper, removedOperation, removedOperationInvocation);
                List<AbstractCodeMapping> additionalExactMatches = new ArrayList<AbstractCodeMapping>();
                List<CallTreeNode> nodesInBreadthFirstOrder = callTree.getNodesInBreadthFirstOrder();
                for (int i = 1; i < nodesInBreadthFirstOrder.size(); i++) {
                    CallTreeNode node = nodesInBreadthFirstOrder.get(i);
                    if (matchingInvocations(node.getInvokedOperation(), operationInvocations, mapper.getOperation1().variableTypeMap()).size() == 0) {
                        UMLOperationBodyMapper nestedMapper = createMapperForInlinedMethod(mapper, node.getInvokedOperation(), node.getInvocation());
                        additionalExactMatches.addAll(nestedMapper.getExactMatches());
                        if (inlineMatchCondition(nestedMapper)) {
                            List<OperationInvocation> nestedMatchingInvocations = matchingInvocations(node.getInvokedOperation(), node.getOriginalOperation().getAllOperationInvocations(), node.getOriginalOperation().variableTypeMap());
                            InlineOperationRefactoring nestedRefactoring = new InlineOperationRefactoring(nestedMapper, mapper.getOperation1(), nestedMatchingInvocations);
                            refactorings.add(nestedRefactoring);
                            operationBodyMapper.addChildMapper(nestedMapper);
                        }
                    }
                }
                if (inlineMatchCondition(operationBodyMapper)) {
                    InlineOperationRefactoring inlineOperationRefactoring = new InlineOperationRefactoring(operationBodyMapper, mapper.getOperation1(), removedOperationInvocations);
                    refactorings.add(inlineOperationRefactoring);
                }
            }
        }
        return refactorings;
    }

    private List<OperationInvocation> matchingInvocations(UMLOperation removedOperation, List<OperationInvocation> operationInvocations, Map<String, UMLType> variableTypeMap) {
        List<OperationInvocation> removedOperationInvocations = new ArrayList<OperationInvocation>();
        for (OperationInvocation invocation : operationInvocations) {
            if (invocation.matchesOperation(removedOperation, variableTypeMap, modelDiff)) {
                removedOperationInvocations.add(invocation);
            }
        }
        return removedOperationInvocations;
    }

    private UMLOperationBodyMapper createMapperForInlinedMethod(UMLOperationBodyMapper mapper,
                                                                UMLOperation removedOperation, OperationInvocation removedOperationInvocation) throws RefactoringMinerTimedOutException {
        List<String> arguments = removedOperationInvocation.getArguments();
        List<String> parameters = removedOperation.getParameterNameList();
        Map<String, String> parameterToArgumentMap = new LinkedHashMap<String, String>();
        //special handling for methods with varargs parameter for which no argument is passed in the matching invocation
        int size = Math.min(arguments.size(), parameters.size());
        for (int i = 0; i < size; i++) {
            parameterToArgumentMap.put(parameters.get(i), arguments.get(i));
        }
        UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, mapper, parameterToArgumentMap, classDiff);
        return operationBodyMapper;
    }

    private void generateCallTree(UMLOperation operation, CallTreeNode parent, CallTree callTree) {
        List<OperationInvocation> invocations = operation.getAllOperationInvocations();
        for (UMLOperation removedOperation : removedOperations) {
            for (OperationInvocation invocation : invocations) {
                if (invocation.matchesOperation(removedOperation, operation.variableTypeMap(), modelDiff)) {
                    if (!callTree.contains(removedOperation)) {
                        CallTreeNode node = new CallTreeNode(operation, removedOperation, invocation);
                        parent.addChild(node);
                        generateCallTree(removedOperation, node, callTree);
                    }
                }
            }
        }
    }

    private List<OperationInvocation> getInvocationsInTargetOperationBeforeInline(UMLOperationBodyMapper mapper) {
        List<OperationInvocation> operationInvocations = mapper.getOperation1().getAllOperationInvocations();
        for (StatementObject statement : mapper.getNonMappedLeavesT1()) {
            ExtractOperationDetection.addStatementInvocations(operationInvocations, statement);
            for (UMLAnonymousClass anonymousClass : classDiff.getRemovedAnonymousClasses()) {
                if (statement.getLocationInfo().subsumes(anonymousClass.getLocationInfo())) {
                    for (UMLOperation anonymousOperation : anonymousClass.getOperations()) {
                        for (OperationInvocation anonymousInvocation : anonymousOperation.getAllOperationInvocations()) {
                            if (!ExtractOperationDetection.containsInvocation(operationInvocations, anonymousInvocation)) {
                                operationInvocations.add(anonymousInvocation);
                            }
                        }
                    }
                }
            }
        }
        return operationInvocations;
    }

    private boolean inlineMatchCondition(UMLOperationBodyMapper operationBodyMapper) {
        int delegateStatements = 0;
        for (StatementObject statement : operationBodyMapper.getNonMappedLeavesT1()) {
            OperationInvocation invocation = statement.invocationCoveringEntireFragment();
            if (invocation != null && invocation.matchesOperation(operationBodyMapper.getOperation1())) {
                delegateStatements++;
            }
        }
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1() - delegateStatements;
        List<AbstractCodeMapping> exactMatchList = operationBodyMapper.getExactMatches();
        int exactMatches = exactMatchList.size();
        return mappings > 0 && (mappings > nonMappedElementsT1 ||
                (exactMatches == 1 && !exactMatchList.get(0).getFragment1().throwsNewException() && nonMappedElementsT1 - exactMatches < 10) ||
                (exactMatches > 1 && nonMappedElementsT1 - exactMatches < 20));
    }

    private boolean invocationMatchesWithAddedOperation(OperationInvocation removedOperationInvocation, Map<String, UMLType> variableTypeMap, List<OperationInvocation> operationInvocationsInNewMethod) {
        if (operationInvocationsInNewMethod.contains(removedOperationInvocation)) {
            for (UMLOperation addedOperation : classDiff.getAddedOperations()) {
                if (removedOperationInvocation.matchesOperation(addedOperation, variableTypeMap, modelDiff)) {
                    return true;
                }
            }
        }
        return false;
    }
}
