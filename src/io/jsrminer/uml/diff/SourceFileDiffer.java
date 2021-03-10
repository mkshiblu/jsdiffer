package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.detection.ExtractOperationDetection;
import io.jsrminer.uml.diff.detection.InlineOperationDetection;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.FunctionUtil;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Diff between two source File?
 */
public class SourceFileDiffer extends BaseDiffer {

    public final SourceFileDiff sourceFileDiff;
    public final UMLModelDiff modelDiff;

    ISourceFile container1;
    ISourceFile container2;

    public SourceFileDiffer(final ISourceFile container1, final ISourceFile container2, final UMLModelDiff modelDiff) {
        this.container1 = container1;
        this.container2 = container2;
        sourceFileDiff = new SourceFileDiff(container1, container2);
        this.modelDiff = modelDiff;
    }

    public SourceFileDiff diff() {

        // Find functiondeclarations
        final FunctionDeclaration[] functions1 = container1.getFunctionDeclarations().toArray(FunctionDeclaration[]::new);
        final FunctionDeclaration[] functions2 = container2.getFunctionDeclarations().toArray(FunctionDeclaration[]::new);

        // Check if the common file has some fds
        if (functions2 != null) {
//            // region Convert common file's fds to hashmap
//            final HashMap<String, FunctionDeclaration> functionMap1 = new LinkedHashMap<>();
//            for (FunctionDeclaration function1 : functions1) {
//                functionMap1.put(function1.getQualifiedName(), function1);
//            }
//
//            final HashMap<String, FunctionDeclaration> functionMap2 = new LinkedHashMap<>();
//            for (FunctionDeclaration function2 : functions2) {
//                functionMap2.put(function2.getQualifiedName(), function2);
//            }

            diff(this.sourceFileDiff/*, functionMap1, functionMap2*/);
        }
        return this.sourceFileDiff;
    }

    /**
     * Diff operations between two files
     */
    private void diff(SourceFileDiff sourceDiff/*, final HashMap<String, FunctionDeclaration> functionMap1
            , final HashMap<String, FunctionDeclaration> functionMap2*/) {
        // Process Annotations
        // Process Inheritance
        reportAddedAndRemovedOperationsIfNotEquals(sourceDiff);
        createBodyMapperForCommonNamedFunctions(sourceDiff);
//        processAttributes();
//        checkForAttributeChanges();
        // processAnonymousFunctions(sourceDiff);
        checkForOperationSignatureChanges(sourceDiff);
        checkForInlinedOperations(sourceDiff);
        checkForExtractedOperations(sourceDiff);
        // Match statements declared inside the body directly
        matchStatements(sourceDiff);
    }

    /**
     * Reports removed and added anonymous classes
     */
    protected void processAnonymousFunctions(SourceFileDiff sourceDiff) {

//        for (IAnonymousFunctionDeclaration umlAnonymousClass : this.source1.getAnonymousFunctionDeclarations()) {
//            if (!this.source2.containsAnonymousWithSameAttributesAndOperations(umlAnonymousClass))
//                this.removedAnonymousClasses.add(umlAnonymousClass);
//        }
//        for (UMLAnonymousClass umlAnonymousClass : nextClass.getAnonymousClassList()) {
//            if (!originalClass.containsAnonymousWithSameAttributesAndOperations(umlAnonymousClass))
//                this.addedAnonymousClasses.add(umlAnonymousClass);
//        }
    }

    protected void createBodyMapperForCommonNamedFunctions(SourceFileDiff sourceDiff) {
        final List<IFunctionDeclaration> functions1 = sourceDiff.getSource1().getFunctionDeclarations();
        final List<IFunctionDeclaration> functions2 = sourceDiff.getSource2().getFunctionDeclarations();
        // First match by equalsQualified
        // (In RM it's equals signature which checks modifiers, qualified name and parameter types
        for (IFunctionDeclaration if1 : functions1) {
            FunctionDeclaration function1 = (FunctionDeclaration) if1;

            IFunctionDeclaration function2 = functions2.stream()
                    .filter(f2 -> FunctionUtil.equalsNameParentQualifiedNameAndParamerNames(f2, function1))
                    .findFirst()
                    .orElse(null);

            if (function2 != null) {
                if (getModelDiff() != null) {
                    List<FunctionBodyMapper> mappers
                            = getModelDiff().findMappersWithMatchingSignature2(function2);
                    if (mappers.size() > 0) {
                        var operation1 = mappers.get(0).getOperation1();
                        if (!FunctionUtil.equalNameAndParameterCount(operation1, function1)//operation1.equalSignature(function1)
                                && getModelDiff().commonlyImplementedOperations(operation1, function2, this)) {
                            if (!sourceDiff.getRemovedOperations().contains(function1)) {
                                sourceDiff.getRemovedOperations().add(function1);
                            }
                            break;
                        }
                    }
                }

                // Map and find refactorings between two functions
                UMLOperationDiff operationDiff = new UMLOperationDiff(function1, (FunctionDeclaration) function2);
                FunctionBodyMapper mapper = new FunctionBodyMapper(operationDiff, sourceDiff);
                operationDiff.setMappings(mapper.getMappings());
                this.sourceFileDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());
                // save the mapper TODO
                this.sourceFileDiff.getBodyMapperList().add(mapper);
            }
        }

        // Second Not qualified but the 2nd file contains the operation
        for (IFunctionDeclaration if1 : functions1) {
            FunctionDeclaration function1 = (FunctionDeclaration) if1;
            IFunctionDeclaration function2 = functions2.stream()
                    .filter(f2 -> FunctionUtil.isEqual(f2, function1))
                    .findFirst()
                    .orElse(null);

            if (function2 != null
                    && !containsMapperForOperation(function1)
                    // && functions2.getOperations().contains(operation)
                    && !sourceDiff.getRemovedOperations().contains(function1)) {

//                int index = functions2.indexOf(operation);
//                int lastIndex = functions2.lastIndexOf(operation);
//                int finalIndex = index;
//                if (index != lastIndex) {
//                    double d1 = operation.getReturnParameter().getType()
//                            .normalizedNameDistance(nextClass.getOperations().get(index).getReturnParameter().getType());
//                    double d2 = operation.getReturnParameter().getType()
//                            .normalizedNameDistance(nextClass.getOperations().get(lastIndex).getReturnParameter().getType());
//                    if (d2 < d1) {
//                        finalIndex = lastIndex;
//                    }
//                }

                UMLOperationDiff operationDiff = new UMLOperationDiff(function1, (FunctionDeclaration) function2);
                FunctionBodyMapper bodyMapper
                        = new FunctionBodyMapper(operationDiff, sourceDiff);
                operationDiff.setMappings(bodyMapper.getMappings());
                sourceDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());
                sourceDiff.getBodyMapperList().add(bodyMapper);
            }
        }

        List<FunctionDeclaration> removedOperationsToBeRemoved = new ArrayList<>();
        List<FunctionDeclaration> addedOperationsToBeRemoved = new ArrayList<>();
        for (FunctionDeclaration removedOperation : sourceDiff.getRemovedOperations()) {
            for (FunctionDeclaration addedOperation : sourceDiff.getAddedOperations()) {
                /*if (removedOperation.equalsIgnoringVisibility(addedOperation)) {
                    UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation, this);
                    UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, operationBodyMapper.getMappings());
                    refactorings.addAll(operationSignatureDiff.getRefactorings());
                    this.addOperationBodyMapper(operationBodyMapper);
                    removedOperationsToBeRemoved.add(removedOperation);
                    addedOperationsToBeRemoved.add(addedOperation);
                } else*/
                if (FunctionUtil.nameEqualsIgnoreCaseAndEqualParameterCount(removedOperation, addedOperation)) {

                    UMLOperationDiff operationDiff = new UMLOperationDiff(removedOperation, addedOperation);
                    FunctionBodyMapper bodyMapper
                            = new FunctionBodyMapper(operationDiff, sourceDiff);
                    operationDiff.setMappings(bodyMapper.getMappings());
                    sourceDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());

                    if (!removedOperation.getName().equals(addedOperation.getName()) &&
                            !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                        RenameOperationRefactoring rename = new RenameOperationRefactoring(bodyMapper);
                        sourceDiff.getRefactoringsBeforePostProcessing().add(rename);
                    }
                    sourceDiff.getBodyMapperList().add(bodyMapper);
                    removedOperationsToBeRemoved.add(removedOperation);
                    addedOperationsToBeRemoved.add(addedOperation);
                }
            }
        }
        sourceDiff.getRemovedOperations().removeAll(removedOperationsToBeRemoved);
        sourceDiff.getAddedOperations().removeAll(addedOperationsToBeRemoved);
    }

    private void checkForOperationSignatureChanges(SourceFileDiff sourceDiff) {
        sourceDiff.setConsistentMethodInvocationRenames(findConsistentMethodInvocationRenames(sourceDiff));

        if (sourceDiff.getRemovedOperations().size() <= sourceDiff.getAddedOperations().size()) {

            for (Iterator<FunctionDeclaration> removedOperationIterator = sourceDiff.getRemovedOperations().iterator(); removedOperationIterator.hasNext(); ) {
                FunctionDeclaration removedOperation = removedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();

                for (Iterator<FunctionDeclaration> addedOperationIterator = sourceDiff.getAddedOperations().iterator(); addedOperationIterator.hasNext(); ) {
                    FunctionDeclaration addedOperation = addedOperationIterator.next();
                    int maxDifferenceInPosition;

//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(sourceDiff.getRemovedOperations().size(), sourceDiff.getAddedOperations().size());
//                    }

                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, sourceDiff);
//                    List<FunctionDeclaration> operationsInsideAnonymousClass = addedOperation.getOperationsInsideAnonymousFunctionDeclarations(container1.added);
//                    for (FunctionDeclaration operationInsideAnonymousClass : operationsInsideAnonymousClass) {
//                        updateMapperSet(mapperSet, removedOperation, operationInsideAnonymousClass, addedOperation, maxDifferenceInPosition);
//                    }
                }
                if (!mapperSet.isEmpty()) {
                    FunctionBodyMapper bestMapper = findBestMapper(mapperSet);
                    if (bestMapper != null) {
                        removedOperation = bestMapper.getOperation1();
                        FunctionDeclaration addedOperation = bestMapper.getOperation2();
                        sourceDiff.getAddedOperations().remove(addedOperation);
                        removedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        sourceDiff.getOperationDiffList().add(operationSignatureDiff);
                        sourceDiff.getRefactoringsBeforePostProcessing().addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.getName().equals(addedOperation.getName()) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            sourceDiff.getRefactoringsBeforePostProcessing().add(rename);
                        }
                        sourceDiff.getBodyMapperList().add(bestMapper);
                    }
                }
            }
        } else {
            for (Iterator<FunctionDeclaration> addedOperationIterator = sourceDiff.getAddedOperations().iterator(); addedOperationIterator.hasNext(); ) {
                FunctionDeclaration addedOperation = addedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();
                for (Iterator<FunctionDeclaration> removedOperationIterator = sourceDiff.getRemovedOperations().iterator(); removedOperationIterator.hasNext(); ) {
                    FunctionDeclaration removedOperation = removedOperationIterator.next();
                    int maxDifferenceInPosition;
//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(sourceDiff.getRemovedOperations().size(), sourceDiff.getAddedOperations().size());
//                    }
                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, sourceDiff);
//                    List<FunctionDeclaration> operationsInsideAnonymousClass = addedOperation.getOperationsInsideAnonymousFunctionDeclarations(container1.addedAnonymousClasses);
//                    for (FunctionDeclaration operationInsideAnonymousClass : operationsInsideAnonymousClass) {
//                        updateMapperSet(mapperSet, removedOperation, operationInsideAnonymousClass, addedOperation, maxDifferenceInPosition);
//                    }
                }
                if (!mapperSet.isEmpty()) {
                    FunctionBodyMapper bestMapper = findBestMapper(mapperSet);
                    if (bestMapper != null) {
                        FunctionDeclaration removedOperation = bestMapper.getOperation1();
                        addedOperation = bestMapper.getOperation2();
                        sourceDiff.getRemovedOperations().remove(removedOperation);
                        addedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        sourceDiff.getOperationDiffList().add(operationSignatureDiff);
                        sourceDiff.getRefactoringsBeforePostProcessing().addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.getName().equals(addedOperation.getName()) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            sourceDiff.getRefactoringsBeforePostProcessing().add(rename);
                        }
                        sourceDiff.getBodyMapperList().add(bestMapper);
                    }
                }
            }
        }
    }

//    private Set<MethodInvocationReplacement> findConsistentMethodInvocationRenames(ContainerDiff sourceDiff) {
//        Set<MethodInvocationReplacement> allConsistentMethodInvocationRenames = new LinkedHashSet<>();
//        Set<MethodInvocationReplacement> allInconsistentMethodInvocationRenames = new LinkedHashSet<>();
//        for (FunctionBodyMapper bodyMapper : sourceDiff.getBodyMapperList()) {
//            Set<MethodInvocationReplacement> methodInvocationRenames = bodyMapper.getMethodInvocationRenameReplacements();
//            ConsistentReplacementDetector.updateRenames(allConsistentMethodInvocationRenames, allInconsistentMethodInvocationRenames,
//                    methodInvocationRenames);
//        }
//        allConsistentMethodInvocationRenames.removeAll(allInconsistentMethodInvocationRenames);
//        return allConsistentMethodInvocationRenames;
//    }


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

//    private void updateMapperSet(TreeSet<FunctionBodyMapper> mapperSet, FunctionDeclaration removedOperation
//            , FunctionDeclaration addedOperation, int differenceInPosition, ContainerDiff sourceDiff) throws RefactoringMinerTimedOutException {
//        FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(new UMLOperationDiff(removedOperation, addedOperation), sourceDiff);
//        operationBodyMapper.map();
//
//        List<CodeFragmentMapping> totalMappings = new ArrayList<>(operationBodyMapper.getMappings());
//        int mappings = operationBodyMapper.mappingsWithoutBlocks();
//        if (mappings > 0) {
//            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation);
//            if (exactMappings(operationBodyMapper)) {
//                mapperSet.add(operationBodyMapper);
//            } else if (mappedElementsMoreThanNonMappedT1AndT2(mappings, operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition) &&
//                    removedOperation.testAnnotationCheck(addedOperation)) {
//                mapperSet.add(operationBodyMapper);
//            } else if (mappedElementsMoreThanNonMappedT2(mappings, operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    isPartOfMethodExtracted(removedOperation, addedOperation) &&
//                    removedOperation.testAnnotationCheck(addedOperation)) {
//                mapperSet.add(operationBodyMapper);
//            } else if (mappedElementsMoreThanNonMappedT1(mappings, operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    isPartOfMethodInlined(removedOperation, addedOperation) &&
//                    removedOperation.testAnnotationCheck(addedOperation)) {
//                mapperSet.add(operationBodyMapper);
//            }
//        } else {
//            for (MethodInvocationReplacement replacement : consistentMethodInvocationRenames) {
//                if (replacement.getInvokedOperationBefore().matchesOperation(removedOperation) &&
//                        replacement.getInvokedOperationAfter().matchesOperation(addedOperation)) {
//                    mapperSet.add(operationBodyMapper);
//                    break;
//                }
//            }
//        }
//        if (totalMappings.size() > 0) {
//            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation);
//            if (singleUnmatchedStatementCallsAddedOperation(operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition)) {
//                mapperSet.add(operationBodyMapper);
//            }
//        }
//    }


//    private void updateMapperSet(TreeSet<FunctionBodyMapper> mapperSet, FunctionDeclaration removedOperation
//            , FunctionDeclaration operationInsideAnonymousClass, FunctionDeclaration addedOperation, int differenceInPosition) throws RefactoringMinerTimedOutException {
//        FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(removedOperation, operationInsideAnonymousClass, this);
//        int mappings = operationBodyMapper.mappingsWithoutBlocks();
//        if(mappings > 0) {
//            int absoluteDifferenceInPosition = computeAbsoluteDifferenceInPositionWithinClass(removedOperation, addedOperation);
//            if(exactMappings(operationBodyMapper)) {
//                mapperSet.add(operationBodyMapper);
//            }
//            else if(mappedElementsMoreThanNonMappedT1AndT2(mappings, operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    compatibleSignatures(removedOperation, addedOperation, absoluteDifferenceInPosition)) {
//                mapperSet.add(operationBodyMapper);
//            }
//            else if(mappedElementsMoreThanNonMappedT2(mappings, operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    isPartOfMethodExtracted(removedOperation, addedOperation)) {
//                mapperSet.add(operationBodyMapper);
//            }
//            else if(mappedElementsMoreThanNonMappedT1(mappings, operationBodyMapper) &&
//                    absoluteDifferenceInPosition <= differenceInPosition &&
//                    isPartOfMethodInlined(removedOperation, addedOperation)) {
//                mapperSet.add(operationBodyMapper);
//            }
//        }
//    }

    private void checkForInlinedOperations(SourceFileDiff sourceDiff) {
        List<FunctionDeclaration> removedOperations = sourceFileDiff.getRemovedOperations();
        List<FunctionDeclaration> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration removedOperation : removedOperations) {
            for (FunctionBodyMapper mapper : sourceDiff.getBodyMapperList()) {
                InlineOperationDetection detection = new InlineOperationDetection(mapper, removedOperations, this.sourceFileDiff/*, this.modelDiff*/);
                List<InlineOperationRefactoring> refs = detection.check(removedOperation);
                for (InlineOperationRefactoring refactoring : refs) {
                    sourceDiff.getRefactoringsBeforePostProcessing().add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    sourceDiff.processMapperRefactorings(operationBodyMapper, sourceDiff.getRefactoringsBeforePostProcessing());
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(removedOperation);
                }
            }
        }
        sourceFileDiff.getRemovedOperations().removeAll(operationsToBeRemoved);
    }


    /**
     * Extract is detected by Checking if the already mapped operations contains any calls to
     * any addedOperations.
     */
    private void checkForExtractedOperations(SourceFileDiff sourceDiff) {
        List<FunctionDeclaration> addedOperations = new ArrayList<>(sourceFileDiff.getAddedOperations());
        List<FunctionDeclaration> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration addedOperation : addedOperations) {
            for (FunctionBodyMapper mapper : sourceDiff.getBodyMapperList()) {
                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, sourceFileDiff/*, modelDiff*/);
                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
                for (ExtractOperationRefactoring refactoring : refs) {
                    sourceDiff.getRefactoringsBeforePostProcessing().add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    sourceDiff.processMapperRefactorings(operationBodyMapper, sourceFileDiff.getRefactoringsBeforePostProcessing());
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(addedOperation);
                }
                checkForInconsistentVariableRenames(mapper, sourceDiff);
            }
        }
        sourceFileDiff.getAddedOperations().removeAll(operationsToBeRemoved);
    }


    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperationsIfNotEquals(SourceFileDiff sourceDiff) {
        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        boolean isEqual;
        for (IFunctionDeclaration function1 : sourceDiff.getSource1().getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function2 : sourceDiff.getSource2().getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function1, function2)) {
                    break;
                }
            }

            // If no match on model2 report as removeed
            if (!isEqual)
                sourceDiff.reportRemovedOperation((FunctionDeclaration) function1);
        }
        for (IFunctionDeclaration function2 : sourceDiff.getSource2().getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function1 : sourceDiff.getSource1().getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function2, function1)) {
                    break;
                }
            }

            // If no match on model1 report as added
            if (!isEqual)
                sourceDiff.reportAddedOperation((FunctionDeclaration) function2);
        }
        // endregion
    }

    /**
     * Returns true if the mapper's operation one is equal to the test operation
     */
    public boolean containsMapperForOperation(FunctionDeclaration operation) {
        for (FunctionBodyMapper mapper : sourceFileDiff.getBodyMapperList()) {
//            if(mapper.getOperation1().equalsQualified(operation)) {
//                return true;
//            }
            if (mapper.getOperation1().equals(operation))
                return true;
        }
        return false;
    }

    private void matchStatements(SourceFileDiff sourceDiff) {

        // Create  two functions using to statemtens
        FunctionDeclaration function1 = createLambda(sourceDiff.getSource1().getStatements(), sourceDiff.getSource1());
        FunctionDeclaration function2 = createLambda(sourceDiff.getSource2().getStatements(), sourceDiff.getSource2());
        // ContainerBodyMapper bodyMapper = new ContainerBodyMapper(container1, container2);
        FunctionBodyMapper mapper = new FunctionBodyMapper(function1, function2);

        int mappings = mapper.mappingsWithoutBlocks();
        if (mappings > 0) {
//            int nonMappedElementsT1 = mapper.nonMappedElementsT1();
//            int nonMappedElementsT2 = mapper.nonMappedElementsT2();
//            if(mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) {
            if (mappings > mapper.nonMappedElementsT1() && mappings > mapper.nonMappedElementsT2()) {
//                this.mappings.addAll(mapper.mappings);
//                this.nonMappedInnerNodesT1.addAll(mapper.nonMappedInnerNodesT1);
//                this.nonMappedInnerNodesT2.addAll(mapper.nonMappedInnerNodesT2);
//                this.nonMappedLeavesT1.addAll(mapper.nonMappedLeavesT1);
//                this.nonMappedLeavesT2.addAll(mapper.nonMappedLeavesT2);
                //this.refactorings.addAll(mapper.getRefactorings());
                sourceDiff.getRefactoringsBeforePostProcessing().addAll(mapper.getRefactoringsByVariableAnalysis());
                sourceDiff.setBodyStatementMapper(mapper);
            }
        }
    }

    private FunctionDeclaration createLambda(List<Statement> statements, ISourceFile sourceFile) {
        FunctionDeclaration functionDeclaration = new FunctionDeclaration();
        BlockStatement block = new BlockStatement();
        block.getStatements().addAll(statements);
        block.setSourceLocation(new SourceLocation());
        FunctionBody body = new FunctionBody(block);
        functionDeclaration.setBody(body);
        functionDeclaration.setSourceLocation(new SourceLocation());
        functionDeclaration.setParentContainerQualifiedName(sourceFile.getDirectoryPath());
        functionDeclaration.setQualifiedName(sourceFile.getQualifiedName());
        functionDeclaration.setName(sourceFile.getName());
        functionDeclaration.getAnonymousFunctionDeclarations().addAll(sourceFile.getAnonymousFunctionDeclarations());
        functionDeclaration.getFunctionDeclarations().addAll(sourceFile.getFunctionDeclarations());
        return functionDeclaration;
    }

    public UMLModelDiff getModelDiff() {
        return modelDiff;
    }
}
