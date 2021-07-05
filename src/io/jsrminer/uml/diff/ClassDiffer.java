package io.jsrminer.uml.diff;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.InlineOperationRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.ClassUtil;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.UMLAttribute;
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

public class ClassDiffer extends ContainerDiffer<IClassDeclaration, ClassDiff> {
    ClassDiff classDiff;
    public ClassDiffer(IClassDeclaration class1, IClassDeclaration class2) {
        super(new ClassDiff(class1, class2));
        this.classDiff = super.containerDiff;
    }

    public ClassDiff diff() {
        super.reportAddedAndRemovedOperations();
        super.createBodyMapperForCommonFunctions();

        // processAnonymousFunctions(sourceDiff);
        checkForOperationSignatureChanges();
        processAttributes();
        checkForAttributeChanges();
        checkForInlinedOperations();
        checkForExtractedOperations();

        return this.containerDiff;
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

    protected void processAttributes() {
        var originalClass = classDiff.getContainer1();
        var nextClass = classDiff.getContainer1();

        for (UMLAttribute attribute : originalClass.getAttributes()) {
            var matchingAttribute = ClassUtil.containsAttribute(nextClass, attribute);
            if (matchingAttribute == null) {
                this.classDiff.reportRemovedAttribute(attribute);
            } else {
                var attributeDiff = new UMLAttributeDiff(attribute, matchingAttribute/*, this, modelDiff*/);
                if (!attributeDiff.isEmpty()) {
                    this.classDiff.getRefactoringsBeforePostProcessing().addAll(attributeDiff.getRefactorings());
                    this.classDiff.getAttributeDiffList().add(attributeDiff);
                }
            }
        }
        for (UMLAttribute attribute : nextClass.getAttributes()) {
            UMLAttribute matchingAttribute = ClassUtil.containsAttribute(originalClass, attribute);
            if (matchingAttribute == null) {
                this.classDiff.reportAddedAttribute(attribute);
            } else {
                var attributeDiff = new UMLAttributeDiff(matchingAttribute, attribute/*, this, modelDiff*/);
                if (!attributeDiff.isEmpty()) {
                    this.classDiff.getRefactoringsBeforePostProcessing().addAll(attributeDiff.getRefactorings());
                    this.classDiff.getAttributeDiffList().add(attributeDiff);
                }
            }
        }
    }

    protected void checkForAttributeChanges() throws RefactoringMinerTimedOutException {
        for (Iterator<UMLAttribute> removedAttributeIterator = classDiff.getRemovedAttributes().iterator(); removedAttributeIterator.hasNext(); ) {
            UMLAttribute removedAttribute = removedAttributeIterator.next();
            for (Iterator<UMLAttribute> addedAttributeIterator = classDiff.getAddedAttributes().iterator(); addedAttributeIterator.hasNext(); ) {
                UMLAttribute addedAttribute = addedAttributeIterator.next();
                if (removedAttribute.getName().equals(addedAttribute.getName())) {
                    UMLAttributeDiff attributeDiff = new UMLAttributeDiff(removedAttribute, addedAttribute/*, this, modelDiff*/);
                    this.classDiff.getRefactoringsBeforePostProcessing().addAll(attributeDiff.getRefactorings());
                    addedAttributeIterator.remove();
                    removedAttributeIterator.remove();
                    this.classDiff.getAttributeDiffList().add(attributeDiff);
                    break;
                }
            }
        }
    }
}
