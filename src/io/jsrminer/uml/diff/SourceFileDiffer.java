package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.ClassUtil;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.FunctionUtil;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Diff between two source File?
 */
public class SourceFileDiffer extends ContainerDiffer<ISourceFile> {

    public final UMLModelDiff modelDiff;

    public SourceFileDiffer(final ISourceFile container1, final ISourceFile container2, final UMLModelDiff modelDiff) {
        super(container1, container2);
        this.modelDiff = modelDiff;
    }

    @Override
    public ContainerDiff<ISourceFile> diff() {
        reportAddedAndRemovedClassDeclarations();
        diffCommonClasses();

        super.diffChildFunctions();
        // Match statements declared inside the body directly
        matchStatements();
        return super.containerDiff;
    }

    @Override
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


    @Override
    protected void matchStatements() {
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
}
