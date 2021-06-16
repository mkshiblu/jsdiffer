package io.jsrminer.uml;

import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.diff.*;
import io.jsrminer.uml.diff.detection.ExtractOperationDetection;
import io.jsrminer.uml.diff.detection.InlineOperationDetection;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ClassDiffer extends BaseDiffer{
    private final IClassDeclaration class1;
    private final IClassDeclaration class2;
    private final ClassDiff classDiff;

    public ClassDiffer(IClassDeclaration class1, IClassDeclaration class2) {
        this.class1 = class1;
        this.class2 = class2;
        this.classDiff = new ClassDiff(class1, class2);
    }

    public ClassDiff diff() {
       // matchStatements();
        diffChildFunctions();
        return this.classDiff;
    }

    /**
     * Diff all the children functions ony
     */
    public void diffChildFunctions() {
        reportAddedAndRemovedOperations();
        createBodyMapperForCommonNamedFunctions();
//        processAttributes();
//        checkForAttributeChanges();
        // processAnonymousFunctions(sourceDiff);
        checkForOperationSignatureChanges();
        checkForInlinedOperations();
        checkForExtractedOperations();
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperations() {
        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        boolean isEqual;
        for (IFunctionDeclaration function1 : this.class1.getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function2 : this.class2.getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function1, function2)) {
                    break;
                }
            }

            // If no match on model2 report as removed
            if (!isEqual)
                classDiff.reportRemovedOperation((FunctionDeclaration) function1);
        }
        for (IFunctionDeclaration function2 : classDiff.getContainer2().getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function1 : classDiff.getContainer1().getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function2, function1)) {
                    break;
                }
            }

            // If no match report as added
            if (!isEqual)
                classDiff.reportAddedOperation((FunctionDeclaration) function2);
        }
    }

    protected void createBodyMapperForCommonNamedFunctions() {
        final List<IFunctionDeclaration> functions1 = classDiff.getContainer1().getFunctionDeclarations();
        final List<IFunctionDeclaration> functions2 = classDiff.getContainer2().getFunctionDeclarations();
        // First match by equalsQualified
        // (In RM it's equals signature which checks modifiers, qualified name and parameter types
        for (IFunctionDeclaration if1 : functions1) {
            FunctionDeclaration function1 = (FunctionDeclaration) if1;

            IFunctionDeclaration function2 = functions2.stream()
                    .filter(f2 -> FunctionUtil.equalsNameParentQualifiedNameAndParamerNames(f2, function1))
                    .findFirst()
                    .orElse(null);

            if (function2 != null) {
//                if (getModelDiff() != null) {
//                    List<FunctionBodyMapper> mappers
//                            = getModelDiff().findMappersWithMatchingSignature2(function2);
//                    if (mappers.size() > 0) {
//                        var operation1 = mappers.get(0).getOperation1();
//                        if (!FunctionUtil.equalNameAndParameterCount(operation1, function1)//operation1.equalSignature(function1)
//                                && getModelDiff().commonlyImplementedOperations(operation1, function2, this)) {
//                            if (!containerDiff.getRemovedOperations().contains(function1)) {
//                                containerDiff.getRemovedOperations().add(function1);
//                            }
//                            break;
//                        }
//                    }
//                }

                // Map and find refactorings between two functions
                UMLOperationDiff operationDiff = new UMLOperationDiff(function1, (FunctionDeclaration) function2);
                FunctionBodyMapper mapper = new FunctionBodyMapper(operationDiff, classDiff);
                operationDiff.setMappings(mapper.getMappings());
                this.classDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());
                // save the mapper TODO
                this.classDiff.getOperationBodyMapperList().add(mapper);
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
                    && !classDiff.getRemovedOperations().contains(function1)) {

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
                        = new FunctionBodyMapper(operationDiff, classDiff);
                operationDiff.setMappings(bodyMapper.getMappings());
                classDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());
                classDiff.getOperationBodyMapperList().add(bodyMapper);
            }
        }

        List<FunctionDeclaration> removedOperationsToBeRemoved = new ArrayList<>();
        List<FunctionDeclaration> addedOperationsToBeRemoved = new ArrayList<>();
        for (FunctionDeclaration removedOperation : classDiff.getRemovedOperations()) {
            for (FunctionDeclaration addedOperation : classDiff.getAddedOperations()) {
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
                            = new FunctionBodyMapper(operationDiff, classDiff);
                    operationDiff.setMappings(bodyMapper.getMappings());
                    classDiff.getRefactoringsBeforePostProcessing().addAll(operationDiff.getRefactorings());

                    if (!removedOperation.getName().equals(addedOperation.getName()) &&
                            !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                        RenameOperationRefactoring rename = new RenameOperationRefactoring(bodyMapper);
                        classDiff.getRefactoringsBeforePostProcessing().add(rename);
                    }
                    classDiff.getOperationBodyMapperList().add(bodyMapper);
                    removedOperationsToBeRemoved.add(removedOperation);
                    addedOperationsToBeRemoved.add(addedOperation);
                }
            }
        }
        classDiff.getRemovedOperations().removeAll(removedOperationsToBeRemoved);
        classDiff.getAddedOperations().removeAll(addedOperationsToBeRemoved);
    }

    private void checkForOperationSignatureChanges() {
        classDiff.setConsistentMethodInvocationRenames(findConsistentMethodInvocationRenames(classDiff));

        if (classDiff.getRemovedOperations().size() <= classDiff.getAddedOperations().size()) {

            for (Iterator<FunctionDeclaration> removedOperationIterator = classDiff.getRemovedOperations().iterator(); removedOperationIterator.hasNext(); ) {
                FunctionDeclaration removedOperation = removedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();

                for (Iterator<FunctionDeclaration> addedOperationIterator = classDiff.getAddedOperations().iterator(); addedOperationIterator.hasNext(); ) {
                    FunctionDeclaration addedOperation = addedOperationIterator.next();
                    int maxDifferenceInPosition;

//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(classDiff.getRemovedOperations().size(), classDiff.getAddedOperations().size());
//                    }

                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, classDiff);
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
                        classDiff.getAddedOperations().remove(addedOperation);
                        removedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        classDiff.getOperationDiffList().add(operationSignatureDiff);
                        classDiff.getRefactoringsBeforePostProcessing().addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.getName().equals(addedOperation.getName()) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            classDiff.getRefactoringsBeforePostProcessing().add(rename);
                        }
                        classDiff.getOperationBodyMapperList().add(bestMapper);
                    }
                }
            }
        } else {
            for (Iterator<FunctionDeclaration> addedOperationIterator = classDiff.getAddedOperations().iterator(); addedOperationIterator.hasNext(); ) {
                FunctionDeclaration addedOperation = addedOperationIterator.next();
                TreeSet<FunctionBodyMapper> mapperSet = new TreeSet<>();
                for (Iterator<FunctionDeclaration> removedOperationIterator = classDiff.getRemovedOperations().iterator(); removedOperationIterator.hasNext(); ) {
                    FunctionDeclaration removedOperation = removedOperationIterator.next();
                    int maxDifferenceInPosition;
//                    if (removedOperation.hasTestAnnotation() && addedOperation.hasTestAnnotation()) {
//                        maxDifferenceInPosition = Math.abs(removedOperations.size() - addedOperations.size());
//                    } else {
                    maxDifferenceInPosition = Math.max(classDiff.getRemovedOperations().size(), classDiff.getAddedOperations().size());
//                    }
                    updateMapperSet(mapperSet, removedOperation, addedOperation, maxDifferenceInPosition, classDiff);
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
                        classDiff.getRemovedOperations().remove(removedOperation);
                        addedOperationIterator.remove();

                        UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, bestMapper.getMappings());
                        classDiff.getOperationDiffList().add(operationSignatureDiff);
                        classDiff.getRefactoringsBeforePostProcessing().addAll(operationSignatureDiff.getRefactorings());
                        if (!removedOperation.getName().equals(addedOperation.getName()) &&
                                !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
                            RenameOperationRefactoring rename = new RenameOperationRefactoring(bestMapper);
                            classDiff.getRefactoringsBeforePostProcessing().add(rename);
                        }
                        classDiff.getOperationBodyMapperList().add(bestMapper);
                    }
                }
            }
        }
    }

    /**
     * Returns true if the mapper's operation one is equal to the test operation
     */
    public boolean containsMapperForOperation(FunctionDeclaration operation) {
        for (FunctionBodyMapper mapper : this.classDiff.getOperationBodyMapperList()) {
//            if(mapper.getOperation1().equalsQualified(operation)) {
//                return true;
//            }
            if (mapper.getOperation1().equals(operation))
                return true;
        }
        return false;
    }

    private void checkForInlinedOperations() {
        List<FunctionDeclaration> removedOperations = classDiff.getRemovedOperations();
        List<FunctionDeclaration> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration removedOperation : removedOperations) {
            for (FunctionBodyMapper mapper : classDiff.getOperationBodyMapperList()) {
                InlineOperationDetection detection = new InlineOperationDetection(mapper, removedOperations, classDiff/*, this.modelDiff*/);
                List<InlineOperationRefactoring> refs = detection.check(removedOperation);
                for (InlineOperationRefactoring refactoring : refs) {
                    classDiff.getRefactoringsBeforePostProcessing().add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    classDiff.processMapperRefactorings(operationBodyMapper, classDiff.getRefactoringsBeforePostProcessing());
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(removedOperation);
                }
            }
        }
        classDiff.getRemovedOperations().removeAll(operationsToBeRemoved);
    }


    /**
     * Extract is detected by Checking if the already mapped operations contains any calls to
     * any addedOperations.
     */
    private void checkForExtractedOperations() {
        List<FunctionDeclaration> addedOperations = new ArrayList<>(classDiff.getAddedOperations());
        List<FunctionDeclaration> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration addedOperation : addedOperations) {
            for (FunctionBodyMapper mapper : classDiff.getOperationBodyMapperList()) {
                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, classDiff/*, modelDiff*/);
                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
                for (ExtractOperationRefactoring refactoring : refs) {
                    classDiff.getRefactoringsBeforePostProcessing().add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    classDiff.processMapperRefactorings(operationBodyMapper, classDiff.getRefactoringsBeforePostProcessing());
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(addedOperation);
                }
                checkForInconsistentVariableRenames(mapper, classDiff);
            }
        }
        classDiff.getAddedOperations().removeAll(operationsToBeRemoved);
    }

    private void matchStatements() {
        // Create  two functions using to statemtens
        var function1 = (FunctionDeclaration) classDiff.getContainer1();
        FunctionDeclaration function2 = (FunctionDeclaration) classDiff.getContainer2();
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
                classDiff.getRefactoringsBeforePostProcessing().addAll(mapper.getRefactoringsByVariableAnalysis());
                classDiff.setBodyStatementMapper(mapper);
            }
        }
    }
}
