package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.FunctionUtil;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.entities.Container;

public class ContainerDiffer {

    private final Container function1;
    private final Container function2;
    private final ContainerDiff containerDiff;

    public ContainerDiffer(Container function1, Container function2) {
        this.function1 = function1;
        this.function2 = function2;
        this.containerDiff = new ContainerDiff(function1, function2);
    }

    /**
     * Diff all the children functions
     */
    public void diffChildFunctions() {
        reportAddedAndRemovedOperations(this.containerDiff);
        createBodyMapperForCommonNamedFunctions(this.containerDiff);
//        processAttributes();
//        checkForAttributeChanges();
        // processAnonymousFunctions(sourceDiff);
//        checkForOperationSignatureChanges(this.containerDiff);
//        checkForInlinedOperations(this.containerDiff);
//        checkForExtractedOperations(this.containerDiff);
//        // Match statements declared inside the body directly
//        matchStatements(this.containerDiff);
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperations(ContainerDiff containerDiff) {
        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        boolean isEqual;
        for (IFunctionDeclaration function1 : containerDiff.container1.getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function2 : containerDiff.container2.getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function1, function2)) {
                    break;
                }
            }

            // If no match on model2 report as removed
            if (!isEqual)
                containerDiff.reportRemovedOperation((FunctionDeclaration) function1);
        }
        for (IFunctionDeclaration function2 : containerDiff.container2.getFunctionDeclarations()) {
            isEqual = false;
            for (IFunctionDeclaration function1 : containerDiff.container1.getFunctionDeclarations()) {
                if (isEqual = FunctionUtil.isEqual(function2, function1)) {
                    break;
                }
            }

            // If no match report as added
            if (!isEqual)
                containerDiff.reportAddedOperation((FunctionDeclaration) function2);
        }
    }

    protected void createBodyMapperForCommonNamedFunctions(ContainerDiff containerDiff) {
//        for(UMLOperation originalOperation : originalClass.getOperations()) {
//            for(UMLOperation nextOperation : nextClass.getOperations()) {
//                if(originalOperation.equalsQualified(nextOperation)) {
//                    if(getModelDiff() != null) {
//                        List<UMLOperationBodyMapper> mappers = getModelDiff().findMappersWithMatchingSignature2(nextOperation);
//                        if(mappers.size() > 0) {
//                            UMLOperation operation1 = mappers.get(0).getOperation1();
//                            if(!operation1.equalSignature(originalOperation) &&
//                                    getModelDiff().commonlyImplementedOperations(operation1, nextOperation, this)) {
//                                if(!removedOperations.contains(originalOperation)) {
//                                    removedOperations.add(originalOperation);
//                                }
//                                break;
//                            }
//                        }
//                    }
//                    UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(originalOperation, nextOperation, this);
//                    UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(originalOperation, nextOperation, operationBodyMapper.getMappings());
//                    refactorings.addAll(operationSignatureDiff.getRefactorings());
//                    this.addOperationBodyMapper(operationBodyMapper);
//                }
//            }
//        }
//        for(UMLOperation operation : originalClass.getOperations()) {
//            if(!containsMapperForOperation1(operation) && nextClass.getOperations().contains(operation) && !removedOperations.contains(operation)) {
//                int index = nextClass.getOperations().indexOf(operation);
//                int lastIndex = nextClass.getOperations().lastIndexOf(operation);
//                int finalIndex = index;
//                if(index != lastIndex) {
//                    if(containsMapperForOperation2(nextClass.getOperations().get(index))) {
//                        finalIndex = lastIndex;
//                    }
//                    else if(!operation.isConstructor()) {
//                        double d1 = operation.getReturnParameter().getType().normalizedNameDistance(nextClass.getOperations().get(index).getReturnParameter().getType());
//                        double d2 = operation.getReturnParameter().getType().normalizedNameDistance(nextClass.getOperations().get(lastIndex).getReturnParameter().getType());
//                        if(d2 < d1) {
//                            finalIndex = lastIndex;
//                        }
//                    }
//                }
//                UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(operation, nextClass.getOperations().get(finalIndex), this);
//                UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(operation, nextClass.getOperations().get(finalIndex), operationBodyMapper.getMappings());
//                refactorings.addAll(operationSignatureDiff.getRefactorings());
//                this.addOperationBodyMapper(operationBodyMapper);
//            }
//        }
//        List<UMLOperation> removedOperationsToBeRemoved = new ArrayList<UMLOperation>();
//        List<UMLOperation> addedOperationsToBeRemoved = new ArrayList<UMLOperation>();
//        for(UMLOperation removedOperation : removedOperations) {
//            for(UMLOperation addedOperation : addedOperations) {
//                if(removedOperation.equalsIgnoringVisibility(addedOperation)) {
//                    UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation, this);
//                    UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, operationBodyMapper.getMappings());
//                    refactorings.addAll(operationSignatureDiff.getRefactorings());
//                    this.addOperationBodyMapper(operationBodyMapper);
//                    removedOperationsToBeRemoved.add(removedOperation);
//                    addedOperationsToBeRemoved.add(addedOperation);
//                }
//                else if(removedOperation.equalsIgnoringNameCase(addedOperation)) {
//                    UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation, this);
//                    UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, operationBodyMapper.getMappings());
//                    refactorings.addAll(operationSignatureDiff.getRefactorings());
//                    if(!removedOperation.getName().equals(addedOperation.getName()) &&
//                            !(removedOperation.isConstructor() && addedOperation.isConstructor())) {
//                        RenameOperationRefactoring rename = new RenameOperationRefactoring(operationBodyMapper);
//                        refactorings.add(rename);
//                    }
//                    this.addOperationBodyMapper(operationBodyMapper);
//                    removedOperationsToBeRemoved.add(removedOperation);
//                    addedOperationsToBeRemoved.add(addedOperation);
//                }
//            }
//        }
//        removedOperations.removeAll(removedOperationsToBeRemoved);
//        addedOperations.removeAll(addedOperationsToBeRemoved);
    }
}
