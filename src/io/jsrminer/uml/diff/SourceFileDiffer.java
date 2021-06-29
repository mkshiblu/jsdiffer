package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.ClassDiffer;
import io.jsrminer.uml.ClassUtil;
import io.jsrminer.uml.diff.detection.ExtractOperationDetection;
import io.jsrminer.uml.diff.detection.InlineOperationDetection;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.FunctionUtil;
import io.rminerx.core.api.IContainer;
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

    public final ContainerDiff<ISourceFile> containerDiff;
    public final UMLModelDiff modelDiff;

    public SourceFileDiffer(final ISourceFile container1, final ISourceFile container2, final UMLModelDiff modelDiff) {
        this.containerDiff = new ContainerDiff<ISourceFile>(container1, container2);
        this.modelDiff = modelDiff;
    }

    public ContainerDiff diff() {

        // Find functiondeclarations
        final FunctionDeclaration[] functions1 = containerDiff.getContainer1().getFunctionDeclarations().toArray(FunctionDeclaration[]::new);
        final FunctionDeclaration[] functions2 = containerDiff.getContainer2().getFunctionDeclarations().toArray(FunctionDeclaration[]::new);

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

            diff(this.containerDiff/*, functionMap1, functionMap2*/);
        }
        return this.containerDiff;
    }

    /**
     * Diff operations between two files
     */
    private void diff(ContainerDiff sourceDiff) {
        // Process Annotations
        // Process Inheritance

        reportAddedAndRemovedClassDeclarations();
        diffCommonClasses();

        reportAddedAndRemovedOperations();
        createBodyMapperForCommonFunctions();
//        processAttributes();
//        checkForAttributeChanges();
        // processAnonymousFunctions(sourceDiff);
        checkForOperationSignatureChanges();
        checkForInlinedOperations();
        checkForExtractedOperations();

        // Match statements declared inside the body directly
        matchStatements();
    }

    void diffCommonClasses() {
        for (var class1 : containerDiff.getContainer1().getClassDeclarations()) {
            for (var class2 : containerDiff.getContainer2().getClassDeclarations()) {
                if (ClassUtil.isEqual(class1, class2)) {
                    // do class diff
                    var classDiffer = new ClassDiffer(class1, class2);
                    var classDiff = classDiffer.diff();

                    if (!classDiff.isEmpty()) {
                        this.containerDiff.reportCommonClassDiffList(classDiff);
                    }
                }
            }
        }
    }

    protected void reportAddedAndRemovedClassDeclarations() {
        boolean isEqual;
        for (var class1 : containerDiff.getContainer1().getClassDeclarations()) {
            isEqual = false;
            for (var class2 : containerDiff.getContainer2().getClassDeclarations()) {
                if (isEqual = ClassUtil.isEqual(class1, class2)) {
                    break;
                }
            }

            // If no match on model2 report as removed
            if (!isEqual)
                containerDiff.reportRemovedClass(class1);
        }

        for (var class2 : containerDiff.getContainer2().getClassDeclarations()) {
            isEqual = false;
            for (var class1 : containerDiff.getContainer1().getClassDeclarations()) {
                if (isEqual = ClassUtil.isEqual(class2, class1)) {
                    break;
                }
            }

            // If no match on model1 report as added
            if (!isEqual)
                containerDiff.reportAddedClass(class2);
        }
    }

    /**
     * Reports removed and added anonymous classes
     */
    protected void processAnonymousFunctions() {

//        for (IAnonymousFunctionDeclaration umlAnonymousClass : this.source1.getAnonymousFunctionDeclarations()) {
//            if (!this.source2.containsAnonymousWithSameAttributesAndOperations(umlAnonymousClass))
//                this.removedAnonymousClasses.add(umlAnonymousClass);
//        }
//        for (UMLAnonymousClass umlAnonymousClass : nextClass.getAnonymousClassList()) {
//            if (!originalClass.containsAnonymousWithSameAttributesAndOperations(umlAnonymousClass))
//                this.addedAnonymousClasses.add(umlAnonymousClass);
//        }
    }

    protected void createBodyMapperForCommonFunctions() {
        final List<IFunctionDeclaration> functions1 = containerDiff.getContainer1().getFunctionDeclarations();
        final List<IFunctionDeclaration> functions2 = containerDiff.getContainer2().getFunctionDeclarations();
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
                            if (!containerDiff.getRemovedOperations().contains(function1)) {
                                containerDiff.getRemovedOperations().add(function1);
                            }
                            break;
                        }
                    }
                }

                // Map and find refactorings between two functions
                UMLOperationDiff operationDiff = new UMLOperationDiff(function1, (FunctionDeclaration) function2);
                FunctionBodyMapper mapper = new FunctionBodyMapper(operationDiff, containerDiff);
                operationDiff.setMappings(mapper.getMappings());
                this.containerDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());
                // save the mapper TODO
                this.containerDiff.getOperationBodyMapperList().add(mapper);
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
                    && !containerDiff.getRemovedOperations().contains(function1)) {

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
                        = new FunctionBodyMapper(operationDiff, containerDiff);
                operationDiff.setMappings(bodyMapper.getMappings());
                containerDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());
                containerDiff.getOperationBodyMapperList().add(bodyMapper);
            }
        }

        List<FunctionDeclaration> removedOperationsToBeRemoved = new ArrayList<>();
        List<FunctionDeclaration> addedOperationsToBeRemoved = new ArrayList<>();
        for (FunctionDeclaration removedOperation : containerDiff.getRemovedOperations()) {
            for (FunctionDeclaration addedOperation : containerDiff.getAddedOperations()) {
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
                            = new FunctionBodyMapper(operationDiff, containerDiff);
                    operationDiff.setMappings(bodyMapper.getMappings());
                    containerDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());

                    if (!removedOperation.getName().equals(addedOperation.getName()) &&
                            !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                        RenameOperationRefactoring rename = new RenameOperationRefactoring(bodyMapper);
                        containerDiff.getRefactoringsBeforePostProcessing().add(rename);
                    }
                    containerDiff.getOperationBodyMapperList().add(bodyMapper);
                    removedOperationsToBeRemoved.add(removedOperation);
                    addedOperationsToBeRemoved.add(addedOperation);
                }
            }
        }
        containerDiff.getRemovedOperations().removeAll(removedOperationsToBeRemoved);
        containerDiff.getAddedOperations().removeAll(addedOperationsToBeRemoved);
    }

    private void checkForOperationSignatureChanges() {
        containerDiff.setConsistentMethodInvocationRenames(findConsistentMethodInvocationRenames(containerDiff));

        if (containerDiff.getRemovedOperations().size() <= containerDiff.getAddedOperations().size()) {

            for (Iterator<FunctionDeclaration> removedOperationIterator = containerDiff.getRemovedOperations().iterator(); removedOperationIterator.hasNext(); ) {
                FunctionDeclaration removedOperation = removedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();

                for (Iterator<FunctionDeclaration> addedOperationIterator = containerDiff.getAddedOperations().iterator(); addedOperationIterator.hasNext(); ) {
                    FunctionDeclaration addedOperation = addedOperationIterator.next();
                    int maxDifferenceInPosition;

//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(containerDiff.getRemovedOperations().size(), containerDiff.getAddedOperations().size());
//                    }

                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, containerDiff);
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
                        containerDiff.getAddedOperations().remove(addedOperation);
                        removedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        containerDiff.getOperationDiffList().add(operationSignatureDiff);
                        containerDiff.getRefactoringsBeforePostProcessing().addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.getName().equals(addedOperation.getName()) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            containerDiff.getRefactoringsBeforePostProcessing().add(rename);
                        }
                        containerDiff.getOperationBodyMapperList().add(bestMapper);
                    }
                }
            }
        } else {
            for (Iterator<FunctionDeclaration> addedOperationIterator = containerDiff.getAddedOperations().iterator(); addedOperationIterator.hasNext(); ) {
                FunctionDeclaration addedOperation = addedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();
                for (Iterator<FunctionDeclaration> removedOperationIterator = containerDiff.getRemovedOperations().iterator(); removedOperationIterator.hasNext(); ) {
                    FunctionDeclaration removedOperation = removedOperationIterator.next();
                    int maxDifferenceInPosition;
//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(containerDiff.getRemovedOperations().size(), containerDiff.getAddedOperations().size());
//                    }
                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, containerDiff);
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
                        containerDiff.getRemovedOperations().remove(removedOperation);
                        addedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        containerDiff.getOperationDiffList().add(operationSignatureDiff);
                        containerDiff.getRefactoringsBeforePostProcessing().addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.getName().equals(addedOperation.getName()) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            containerDiff.getRefactoringsBeforePostProcessing().add(rename);
                        }
                        containerDiff.getOperationBodyMapperList().add(bestMapper);
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

    private void checkForInlinedOperations() {
        List<FunctionDeclaration> removedOperations = containerDiff.getRemovedOperations();
        List<FunctionDeclaration> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration removedOperation : removedOperations) {
            for (FunctionBodyMapper mapper : containerDiff.getOperationBodyMapperList()) {
                InlineOperationDetection detection = new InlineOperationDetection(mapper, removedOperations, this.containerDiff/*, this.modelDiff*/);
                List<InlineOperationRefactoring> refs = detection.check(removedOperation);
                for (InlineOperationRefactoring refactoring : refs) {
                    containerDiff.getRefactoringsBeforePostProcessing().add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    containerDiff.processMapperRefactorings(operationBodyMapper, containerDiff.getRefactoringsBeforePostProcessing());
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(removedOperation);
                }
            }
        }
        containerDiff.getRemovedOperations().removeAll(operationsToBeRemoved);
    }


    /**
     * Extract is detected by Checking if the already mapped operations contains any calls to
     * any addedOperations.
     */
    private void checkForExtractedOperations() {
        List<FunctionDeclaration> addedOperations = new ArrayList<>(containerDiff.getAddedOperations());
        List<FunctionDeclaration> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration addedOperation : addedOperations) {
            for (FunctionBodyMapper mapper : containerDiff.getOperationBodyMapperList()) {
                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, containerDiff/*, modelDiff*/);
                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
                for (ExtractOperationRefactoring refactoring : refs) {
                    containerDiff.getRefactoringsBeforePostProcessing().add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    containerDiff.processMapperRefactorings(operationBodyMapper, containerDiff.getRefactoringsBeforePostProcessing());
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(addedOperation);
                }
                checkForInconsistentVariableRenames(mapper, containerDiff);
            }
        }
        containerDiff.getAddedOperations().removeAll(operationsToBeRemoved);
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperations() {
        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        boolean isEqual;
        for (IFunctionDeclaration function1 : containerDiff.getContainer1().getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function2 : containerDiff.getContainer2().getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function1, function2)) {
                    break;
                }
            }

            // If no match on model2 report as removeed
            if (!isEqual)
                containerDiff.reportRemovedOperation((FunctionDeclaration) function1);
        }
        for (IFunctionDeclaration function2 : containerDiff.getContainer2().getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function1 : containerDiff.getContainer1().getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function2, function1)) {
                    break;
                }
            }

            // If no match on model1 report as added
            if (!isEqual)
                containerDiff.reportAddedOperation((FunctionDeclaration) function2);
        }
        // endregion
    }

    /**
     * Returns true if the mapper's operation one is equal to the test operation
     */
    public boolean containsMapperForOperation(FunctionDeclaration operation) {
        for (FunctionBodyMapper mapper : containerDiff.getOperationBodyMapperList()) {
//            if(mapper.getOperation1().equalsQualified(operation)) {
//                return true;
//            }
            if (mapper.getOperation1().equals(operation))
                return true;
        }
        return false;
    }

    private void matchStatements() {

        // Create  two functions using to statemtens
        FunctionDeclaration function1 = createLambda(containerDiff.getContainer1().getStatements(), containerDiff.getContainer1());
        FunctionDeclaration function2 = createLambda(containerDiff.getContainer2().getStatements(), containerDiff.getContainer2());
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
                //sourceDiff.getRefactoringsBeforePostProcessing().addAll(mapper.getRefactoringsAfterPostProcessing());
                containerDiff.getRefactoringsBeforePostProcessing().addAll(mapper.getRefactoringsByVariableAnalysis());
                containerDiff.setBodyStatementMapper(mapper);
            }
        }
    }

    private FunctionDeclaration createLambda(List<Statement> statements, IContainer sourceFile) {
        FunctionDeclaration functionDeclaration = new FunctionDeclaration();
        BlockStatement block = new BlockStatement();
        block.getStatements().addAll(statements);
        block.setSourceLocation(new SourceLocation());
        FunctionBody body = new FunctionBody(block);
        functionDeclaration.setBody(body);
        functionDeclaration.setSourceLocation(new SourceLocation());
        functionDeclaration.setParentContainerQualifiedName(sourceFile.getParentContainerQualifiedName());
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
