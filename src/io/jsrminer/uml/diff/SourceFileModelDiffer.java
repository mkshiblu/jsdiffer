package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.detection.ConsistentReplacementDetector;
import io.jsrminer.uml.diff.detection.ExtractOperationDetection;
import io.jsrminer.uml.diff.detection.InlineOperationDetection;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.InvocationCoverage;
import io.jsrminer.uml.mapping.replacement.MethodInvocationReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class SourceFileModelDiffer {
    public static final double MAX_OPERATION_NAME_DISTANCE = 0.4;

    public final SourceFileModel source1;
    public final SourceFileModel source2;
    public final SourceFileModelDiff sourceDiff;
    public final UMLModelDiff modelDiff;

    //private final Map<String, FunctionBodyMapper> functionBodyMappers = new HashMap<>();
    protected List<IRefactoring> refactorings = new ArrayList<>();
    private List<FunctionBodyMapper> bodyMappers = new ArrayList<>();

    public SourceFileModelDiffer(final SourceFileModel source1, final SourceFileModel source2, final UMLModelDiff modelDiff) {
        this.source1 = source1;
        this.source2 = source2;
        sourceDiff = new SourceFileModelDiff(source1, source2);
        this.modelDiff = modelDiff;
    }

    public SourceFileModelDiff diff() {

        // Find functiondeclarations
        final FunctionDeclaration[] functions1 = source1.getFunctionDeclarations();
        final FunctionDeclaration[] functions2 = source2.getFunctionDeclarations();

        // Check if the common file has some fds
        if (functions2 != null) {
            // region Convert common file's fds to hashmap
            final HashMap<String, FunctionDeclaration> functionMap1 = new LinkedHashMap<>();
            for (FunctionDeclaration function1 : functions1) {
                functionMap1.put(function1.qualifiedName, function1);
            }

            final HashMap<String, FunctionDeclaration> functionMap2 = new LinkedHashMap<>();
            for (FunctionDeclaration function2 : functions2) {
                functionMap2.put(function2.qualifiedName, function2);
            }

            diffOperations(this.sourceDiff, functionMap1, functionMap2);
        }
        return this.sourceDiff;
    }

    /**
     * Diff operations between two common files
     */
    protected void diffOperations(SourceFileModelDiff sourceDiff, final HashMap<String, FunctionDeclaration> functionMap1, final HashMap<String, FunctionDeclaration> functionMap2) {
        // Process Annotations
        // Process Inheritance
        reportAddedAndRemovedOperations(sourceDiff, functionMap1, functionMap2);
        createBodyMapperForCommonNamedFunctions(sourceDiff, functionMap1, functionMap2);
//        processAttributes();
//        checkForAttributeChanges();
//        processAnonymousClasses();
        checkForOperationSignatureChanges(sourceDiff);
        checkForInlinedOperations();
        checkForExtractedOperations();
    }

    private void checkForOperationSignatureChanges(SourceFileModelDiff sourceDiff) {

        List<FunctionDeclaration> removedOperations = new ArrayList<>(sourceDiff.getRemovedOperations().values());
        List<FunctionDeclaration> addedOperations = new ArrayList<>(sourceDiff.getAddedOperations().values());

        if (removedOperations.size() <= addedOperations.size()) {
            for (Iterator<FunctionDeclaration> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext(); ) {
                FunctionDeclaration removedOperation = removedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>((Comparator<FunctionBodyMapper>) (o1, o2) -> {
                    int thisOperationNameEditDistance = o1.operationNameEditDistance();
                    int otherOperationNameEditDistance = o2.operationNameEditDistance();
                    if(thisOperationNameEditDistance != otherOperationNameEditDistance)
                        return Integer.compare(thisOperationNameEditDistance, otherOperationNameEditDistance);
                    else
                        return o1.compareTo(o2);
                });

                for (Iterator<FunctionDeclaration> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext(); ) {
                    FunctionDeclaration addedOperation = addedOperationIterator.next();
                    int maxDifferenceInPosition;

                    // TODO test
//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(removedOperations.size(), addedOperations.size());
                    //}
                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, sourceDiff);

                    // TODO operationsInsideAnonymousClass
//                    List<FunctionDeclaration> operationsInsideAnonymousClass = addedOperation.getOperationsInsideAnonymousClass(this.addedAnonymousClasses);
//                    for (FunctionDeclaration operationInsideAnonymousClass : operationsInsideAnonymousClass) {
//                        updateMapperSet(mapperSet, removedOperation, operationInsideAnonymousClass, addedOperation, maxDifferenceInPosition);
//                    }
                }


                if (!mapperSet.isEmpty()) {
                    FunctionBodyMapper bestMapper = findBestMapper(mapperSet);
                    if (bestMapper != null) {
                        removedOperation = bestMapper.function1;//.getOperation1();
                        FunctionDeclaration addedOperation = bestMapper.function2;//.getOperation2();
                        addedOperations.remove(addedOperation);
                        removedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        //operationDiffList.add(operationSignatureDiff);
                        refactorings.addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.name.equals(addedOperation.name) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            refactorings.add(rename);
                        }
                        this.bodyMappers.add(bestMapper);
                    }
                }
            }
        } else {
            for (Iterator<FunctionDeclaration> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext(); ) {
                FunctionDeclaration addedOperation = addedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();
                for (Iterator<FunctionDeclaration> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext(); ) {
                    FunctionDeclaration removedOperation = removedOperationIterator.next();
                    int maxDifferenceInPosition;
//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(removedOperations.size(), addedOperations.size());
                    //}
                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, sourceDiff);
//                    List<FunctionDeclaration> operationsInsideAnonymousClass = addedOperation.getOperationsInsideAnonymousClass(this.addedAnonymousClasses);
//                    for (FunctionDeclaration operationInsideAnonymousClass : operationsInsideAnonymousClass) {
//                        updateMapperSet(mapperSet, removedOperation, operationInsideAnonymousClass, addedOperation, maxDifferenceInPosition);
//                    }
                }
                if (!mapperSet.isEmpty()) {
                    FunctionBodyMapper bestMapper = findBestMapper(mapperSet);
                    if (bestMapper != null) {
                        FunctionDeclaration removedOperation = bestMapper.function1;
                        addedOperation = bestMapper.function2;
                        removedOperations.remove(removedOperation);
                        addedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());

                        //operationDiffList.add(operationSignatureDiff);
                        refactorings.addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.name.equals(addedOperation.name) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            refactorings.add(rename);
                        }
                        this.bodyMappers.add(bestMapper);
                    }
                }
            }
        }
    }

    private FunctionBodyMapper findBestMapper(TreeSet<FunctionBodyMapper> mapperSet) {
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
//    public boolean equalReturnParameter(FunctionDeclaration function1, FunctionDeclaration function2) {
//
//        // TODO return
////        UMLParameter thisReturnParameter = function1.
////        UMLParameter otherReturnParameter = operation.getReturnParameter();
////        if(thisReturnParameter != null && otherReturnParameter != null)
////            return thisReturnParameter.equals(otherReturnParameter);
////        else if(thisReturnParameter == null && otherReturnParameter == null)
////            return true;
////        else
//            return false;
//    }

    private void updateMapperSet(TreeSet<FunctionBodyMapper> mapperSet, FunctionDeclaration removedOperation
            , FunctionDeclaration addedOperation, int differenceInPosition, SourceFileModelDiff sourceDiff) {
        FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(removedOperation, addedOperation, sourceDiff);
        operationBodyMapper.map();

        List<CodeFragmentMapping> totalMappings = new ArrayList<>(operationBodyMapper.getMappings());
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        if (mappings > 0) {
            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation);
            if (exactMappings(operationBodyMapper, sourceDiff)) {
                mapperSet.add(operationBodyMapper);
            } else if (mappedElementsMoreThanNonMappedT1AndT2(mappings, operationBodyMapper)
                    && absoluteDifferenceInPosition <= differenceInPosition
                    && compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition)
                //&& removedOperation.testAnnotationCheck(addedOperation)
            ) {
                mapperSet.add(operationBodyMapper);
            } else if (mappedElementsMoreThanNonMappedT2(mappings, operationBodyMapper
                    , new ArrayList<>(sourceDiff.getAddedOperations().values())
            ) && absoluteDifferenceInPosition <= differenceInPosition
                    && isPartOfMethodExtracted(removedOperation, addedOperation, sourceDiff.getAddedOperations())
                //        && removedOperation.testAnnotationCheck(addedOperation)
            ) {
                mapperSet.add(operationBodyMapper);
            } else if (mappedElementsMoreThanNonMappedT1(mappings, operationBodyMapper
                    , new ArrayList<>(sourceDiff.getRemovedOperations().values()))
                    && absoluteDifferenceInPosition <= differenceInPosition
                    && isPartOfMethodInlined(removedOperation, addedOperation, sourceDiff.getRemovedOperations())
                //                && removedOperation.testAnnotationCheck(addedOperation)
            ) {
                mapperSet.add(operationBodyMapper);
            }
        } else {

            Set<MethodInvocationReplacement> consistentMethodInvocationRenames = findConsistentMethodInvocationRenames();
            for (MethodInvocationReplacement replacement : consistentMethodInvocationRenames) {
                if (replacement.getInvokedOperationBefore().matchesOperation(removedOperation) &&
                        replacement.getInvokedOperationAfter().matchesOperation(addedOperation)) {
                    mapperSet.add(operationBodyMapper);
                    break;
                }
            }
        }
        if (totalMappings.size() > 0) {
            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation);
            if (singleUnmatchedStatementCallsAddedOperation(operationBodyMapper, sourceDiff) &&
                    absoluteDifferenceInPosition <= differenceInPosition &&
                    compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition)) {
                mapperSet.add(operationBodyMapper);
            }
        }
    }

    private boolean isPartOfMethodExtracted(FunctionDeclaration removedOperation, FunctionDeclaration addedOperation
            , Map<String, FunctionDeclaration> addedOperations) {
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
                for (FunctionDeclaration operation : addedOperations.values()) {
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
        for (Iterator<OperationInvocation> operationInvocationIterator = removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.iterator(); operationInvocationIterator.hasNext(); ) {
            OperationInvocation invocation = operationInvocationIterator.next();
            if (invocation.getFunctionName().startsWith("get")) {
                operationInvocationIterator.remove();
            }
        }
        int numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations = newIntersection.size();
        int numberOfInvocationsMissingFromRemovedOperationWithoutThoseFoundInOtherAddedOperations = numberOfInvocationsMissingFromRemovedOperation - numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations;
        return numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations > numberOfInvocationsMissingFromRemovedOperationWithoutThoseFoundInOtherAddedOperations ||
                numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations > removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.size();
    }

    private boolean isPartOfMethodInlined(FunctionDeclaration removedOperation, FunctionDeclaration addedOperation, Map<String, FunctionDeclaration> removedOperations) {
        List<OperationInvocation> removedOperationInvocations = removedOperation.getBody().getAllOperationInvocations();
        List<OperationInvocation> addedOperationInvocations = addedOperation.getBody().getAllOperationInvocations();
        Set<OperationInvocation> intersection = new LinkedHashSet<>(removedOperationInvocations);
        intersection.retainAll(addedOperationInvocations);
        int numberOfInvocationsMissingFromAddedOperation = new LinkedHashSet<>(addedOperationInvocations).size() - intersection.size();

        Set<OperationInvocation> operationInvocationsInMethodsCalledByRemovedOperation = new LinkedHashSet<>();
        for (OperationInvocation removedOperationInvocation : removedOperationInvocations) {
            if (!intersection.contains(removedOperationInvocation)) {
                for (FunctionDeclaration operation : removedOperations.values()) {
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


    private boolean mappedElementsMoreThanNonMappedT1AndT2(int mappings, FunctionBodyMapper operationBodyMapper) {
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1();
        int nonMappedElementsT2 = operationBodyMapper.nonMappedElementsT2();
        return (mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) ||
                (nonMappedElementsT1 == 0 && mappings > Math.floor(nonMappedElementsT2 / 2.0)) ||
                (mappings == 1 && nonMappedElementsT1 + nonMappedElementsT2 == 1 && operationBodyMapper
                        .function1.name.equals(operationBodyMapper.function2.name));
    }

    private boolean mappedElementsMoreThanNonMappedT2(int mappings, FunctionBodyMapper operationBodyMapper, List<FunctionDeclaration> addedOperations) {
        int nonMappedElementsT2 = operationBodyMapper.nonMappedElementsT2();
        int nonMappedElementsT2CallingAddedOperation = operationBodyMapper.nonMappedElementsT2CallingAddedOperation(addedOperations);
        int nonMappedElementsT2WithoutThoseCallingAddedOperation = nonMappedElementsT2 - nonMappedElementsT2CallingAddedOperation;
        return mappings > nonMappedElementsT2 || (mappings >= nonMappedElementsT2WithoutThoseCallingAddedOperation &&
                nonMappedElementsT2CallingAddedOperation >= nonMappedElementsT2WithoutThoseCallingAddedOperation);
    }

    private boolean mappedElementsMoreThanNonMappedT1(int mappings, FunctionBodyMapper operationBodyMapper, List<FunctionDeclaration> removedOperations) {
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

    private boolean singleUnmatchedStatementCallsAddedOperation(FunctionBodyMapper operationBodyMapper, SourceFileModelDiff sourceDiff) {
        Set<SingleStatement> nonMappedLeavesT1 = operationBodyMapper.getNonMappedLeavesT1();
        Set<SingleStatement> nonMappedLeavesT2 = operationBodyMapper.getNonMappedLeavesT2();
        if (nonMappedLeavesT1.size() == 1 && nonMappedLeavesT2.size() == 1) {
            SingleStatement statementT2 = nonMappedLeavesT2.iterator().next();
            OperationInvocation invocationT2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statementT2);
            if (invocationT2 != null) {
                for (FunctionDeclaration addedOperation : sourceDiff.getAddedOperations().values()) {
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
            , FunctionDeclaration addedOperation, int absoluteDifferenceInPosition) {

        // TODO addedOperation.compatibleSignature
        boolean isCompatibleSignature = removedOperation.getParameters().size() == addedOperation.getParameters().size()
                && addedOperation.getParameters().keySet().equals(removedOperation.getParameters().keySet());

        return isCompatibleSignature ||
                (
                        (absoluteDifferenceInPosition == 0 || operationsBeforeAndAfterMatch(removedOperation, addedOperation)) &&
                                /*!gettersWithDifferentReturnType(removedOperation, addedOperation) &&*/
                                ( /*addedOperation.getParameterTypeList().equals(removedOperation.getParameterTypeList()*/
                                        addedOperation.getParameters().size() == removedOperation.getParameters().size())
                                || normalizedNameDistance(removedOperation, addedOperation) <= MAX_OPERATION_NAME_DISTANCE
                );
    }

    private boolean operationsBeforeAndAfterMatch(FunctionDeclaration removedOperation, FunctionDeclaration addedOperation) {
        FunctionDeclaration operationBefore1 = null;
        FunctionDeclaration operationAfter1 = null;
        FunctionDeclaration[] originalClassOperations = source1.getFunctionDeclarations();


        int removedOperationIndex = ArrayUtils.indexOf(source1.getFunctionDeclarations(), removedOperation);

        if (removedOperationIndex > 0) {
            operationBefore1 = originalClassOperations[removedOperationIndex - 1];
        }

        if (removedOperationIndex < originalClassOperations.length - 1) {
            operationAfter1 = originalClassOperations[removedOperationIndex + 1];
        }

        FunctionDeclaration[] nextClassOperations = source2.getFunctionDeclarations();
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
            operationsBeforeMatch = operationBefore1.name.equals(operationBefore2.name) &&
                    operationBefore1.getParameters().size() == operationBefore2.getParameters().size();
//              operationBefore1.equalParameterTypes(operationBefore2)
//                    && operationBefore1.getName().equals(operationBefore2.getName());
        }

        boolean operationsAfterMatch = false;
        if (operationAfter1 != null && operationAfter2 != null) {

            operationsAfterMatch = operationAfter1.name.equals(operationAfter2.name) &&
                    operationAfter1.getParameters().size() == operationAfter2.getParameters().size();
            //operationsAfterMatch = operationAfter1.equalParameterTypes(operationAfter2) && operationAfter1.getName().equals(operationAfter2.getName());
        }

        return operationsBeforeMatch || operationsAfterMatch;
    }

    private int computeAbsoluteDifferenceInPositionWithinClass(FunctionDeclaration removedOperation, FunctionDeclaration addedOperation) {
        int index1 = ArrayUtils.indexOf(source1.getFunctionDeclarations(), removedOperation);
        int index2 = ArrayUtils.indexOf(source2.getFunctionDeclarations(), addedOperation);
        return Math.abs(index1 - index2);
    }

    private Set<MethodInvocationReplacement> findConsistentMethodInvocationRenames() {
        Set<MethodInvocationReplacement> allConsistentMethodInvocationRenames = new LinkedHashSet<>();
        Set<MethodInvocationReplacement> allInconsistentMethodInvocationRenames = new LinkedHashSet<>();
        for (FunctionBodyMapper bodyMapper : this.bodyMappers) {
            Set<MethodInvocationReplacement> methodInvocationRenames = bodyMapper.getMethodInvocationRenameReplacements();
            ConsistentReplacementDetector.updateRenames(allConsistentMethodInvocationRenames, allInconsistentMethodInvocationRenames,
                    methodInvocationRenames);
        }
        allConsistentMethodInvocationRenames.removeAll(allInconsistentMethodInvocationRenames);
        return allConsistentMethodInvocationRenames;
    }

    private void checkForInlinedOperations() {
        List<FunctionDeclaration> removedOperations = new ArrayList<>(sourceDiff.getRemovedOperations().values());
        List<String> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration removedOperation : removedOperations) {
            for (FunctionBodyMapper mapper : this.bodyMappers) {
                InlineOperationDetection detection = new InlineOperationDetection(mapper, removedOperations, this.sourceDiff, this.modelDiff);
                List<InlineOperationRefactoring> refs = detection.check(removedOperation);
                for (InlineOperationRefactoring refactoring : refs) {
                    refactorings.add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    //processMapperRefactorings(operationBodyMapper, refactorings);
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(removedOperation.name);
                }
            }
        }
        for (String key : operationsToBeRemoved) {
            sourceDiff.getRemovedOperations().remove(key);
        }
//
//        for (Iterator<FunctionDeclaration> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext(); ) {
//            UMLOperation removedOperation = removedOperationIterator.next();
//            for (UMLOperationBodyMapper mapper : getOperationBodyMapperList()) {
//                InlineOperationDetection detection = new InlineOperationDetection(mapper, removedOperations, this, modelDiff);
//                List<InlineOperationRefactoring> refs = detection.check(removedOperation);
//                for (InlineOperationRefactoring refactoring : refs) {
//                    refactorings.add(refactoring);
//                    UMLOperationBodyMapper operationBodyMapper = refactoring.getBodyMapper();
//                    processMapperRefactorings(operationBodyMapper, refactorings);
//                    mapper.addChildMapper(operationBodyMapper);
//                    operationsToBeRemoved.add(removedOperation);
//                }
//            }
//        }
//        removedOperations.removeAll(operationsToBeRemoved);
    }

    private boolean exactMappings(FunctionBodyMapper operationBodyMapper, SourceFileModelDiff sourceDiff) {
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
                        for (String parameterName : addedOperation.getParameters().keySet()) {
                            if (statement.getVariableDeclaration(parameterName) != null) {
                                parameterizedVariableDeclarationStatements++;
                                break;
                            }
                        }
                        countableStatements++;
                    }
                }
                int nonMappedLeavesExactlyMatchedInTheBodyOfAddedOperation = 0;
                for (FunctionDeclaration operation : sourceDiff.getAddedOperations().values()) {
                    if (!operation.equals(addedOperation) && operation.getBody() != null) {
                        for (SingleStatement statement : operation.getBody().blockStatement.getAllLeafStatementsIncludingNested()) {
                            if (nonMappedLeavesT1.contains(statement.getText())) {
                                nonMappedLeavesExactlyMatchedInTheBodyOfAddedOperation++;
                            }
                        }
                    }
                }
                return (countableStatements == parameterizedVariableDeclarationStatements || countableStatements == nonMappedLeavesExactlyMatchedInTheBodyOfAddedOperation + parameterizedVariableDeclarationStatements) && countableStatements > 0;
            } else if (operationBodyMapper.nonMappedElementsT1() == 0 && operationBodyMapper.nonMappedElementsT2() > 0 && operationBodyMapper.getNonMappedInnerNodesT2().size() == 0) {
                int countableStatements = 0;
                int parameterizedVariableDeclarationStatements = 0;
                FunctionDeclaration removedOperation = operationBodyMapper.function1;
                for (SingleStatement statement : operationBodyMapper.getNonMappedLeavesT2()) {
                    if (statement.countableStatement()) {
                        for (String parameterName : removedOperation.getParameters().keySet()) {
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
                        for (String parameterName : removedOperation.getParameters().keySet()) {
                            OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement);
                            if (invocation != null && invocation.getExpression() != null && invocation.getExpression().equals(parameterName)) {
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
                        for (String parameterName : addedOperation.getParameters().keySet()) {
                            OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement);
                            if (invocation != null && invocation.getExpression() != null && invocation.getExpression().equals(parameterName)) {
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
            if (mapping.containsReplacement(Replacement.ReplacementType.TYPE)) {
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

    /**
     * Extract is detected by Checking if the already mapped operations contains any calls to
     * any addedOperations.
     */
    private void checkForExtractedOperations() {
        List<FunctionDeclaration> addedOperations = new ArrayList<>(sourceDiff.getAddedOperations().values());
        List<String> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration addedOperation : addedOperations) {
            for (FunctionBodyMapper mapper : this.bodyMappers) {
                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, sourceDiff, modelDiff);
                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
                for (ExtractOperationRefactoring refactoring : refs) {
                    refactorings.add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    //  processMapperRefactorings(operationBodyMapper, refactorings);
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(addedOperation.name);
                }
                //checkForInconsistentVariableRenames(mapper);
            }
        }
//
//        for (Iterator<FunctionDeclaration> addedOperationIterator = addedOperations.iterator();
//             addedOperationIterator.hasNext(); ) {
//            FunctionDeclaration addedOperation = addedOperationIterator.next();
//
//            for (FunctionBodyMapper mapper : this.bodyMappers) {
//                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, this, sourceDiff);
//                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
//                for (ExtractOperationRefactoring refactoring : refs) {
//                    refactorings.add(refactoring);
//                    UMLOperationBodyMapper operationBodyMapper = refactoring.getBodyMapper();
//                    processMapperRefactorings(operationBodyMapper, refactorings);
//                    mapper.addChildMapper(operationBodyMapper);
//                    operationsToBeRemoved.add(addedOperation);
//                }
//                checkForInconsistentVariableRenames(mapper);
//            }
//        }
        for (String key : operationsToBeRemoved) {
            sourceDiff.getAddedOperations().remove(key);
        }
    }


    protected void createBodyMapperForCommonNamedFunctions(SourceFileModelDiff sourceDiff
            , final HashMap<String, FunctionDeclaration> functionMap1
            , final HashMap<String, FunctionDeclaration> functionMap2) {

        // First map by fully qualified name? TODO revisit
        for (FunctionDeclaration function1 : functionMap1.values()) {
            final FunctionDeclaration function2 = functionMap2.get(function1.qualifiedName);
            // If function exists in both file, try to match their statements
            if (function2 != null) {

                UMLOperationDiff operationDiff = new UMLOperationDiff(function1, function2);

                FunctionBodyMapper mapper = new FunctionBodyMapper(operationDiff, sourceDiff);
                mapper.map();

                this.refactorings.addAll(operationDiff.getRefactorings());

                // save the mapper TODO
                this.bodyMappers.add(mapper);
            }
        }

        for (FunctionDeclaration function1 : functionMap1.values()) {
            // Not qualified but contains the function in the same index?
//            if (!this.functionBodyMappers.containsKey(function1.qualifiedName)
//                    && !sourceDiff.isRemovedOperation(function1.name)) {
//// TODO
//
//            }
        }
    }

    public double normalizedNameDistance(FunctionDeclaration operation1, FunctionDeclaration operation2) {
        String s1 = operation1.name.toLowerCase();
        String s2 = operation2.name.toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperations(SourceFileModelDiff sourceDiff, final HashMap<String, FunctionDeclaration> functionMap1, final HashMap<String, FunctionDeclaration> functionMap2) {
        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        for (FunctionDeclaration fd1 : functionMap1.values()) {
            if (!functionMap2.containsKey(fd1.qualifiedName)) {
                sourceDiff.reportRemovedOperation(fd1);
            }
        }

        for (FunctionDeclaration fd2 : functionMap2.values()) {
            if (!functionMap1.containsKey(fd2.qualifiedName)) {
                sourceDiff.reportAddedOperation(fd2);
            }
        }
        // endregion
    }


    public List<IRefactoring> getRefactorings() {
        return this.refactorings;
    }
}
