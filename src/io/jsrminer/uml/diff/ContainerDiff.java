package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.CandidateAttributeRefactoring;
import io.jsrminer.refactorings.CandidateMergeVariableRefactoring;
import io.jsrminer.refactorings.CandidateSplitVariableRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.MapperRefactoringProcessor;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.MergeVariableReplacement;
import io.jsrminer.uml.mapping.replacement.MethodInvocationReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.SplitVariableReplacement;
import io.rminerx.core.api.IContainer;

import java.util.*;

public class ContainerDiff {

    protected final List<IRefactoring> refactorings = new ArrayList<>();
    protected Set<MethodInvocationReplacement> consistentMethodInvocationRenames;

    protected Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap = new LinkedHashMap<>();
    protected Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap = new LinkedHashMap<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>>();
    protected Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap = new LinkedHashMap<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>>();

    protected final List<FunctionBodyMapper> bodyMapperList = new ArrayList<>();
    private List<UMLOperationDiff> operationDiffList = new ArrayList<>();

    /**
     * Name map
     */
//    private final List<FunctionDeclaration> addedOperations = new ArrayList<>();
//    private final List<FunctionDeclaration> removedOperations = new ArrayList<>();

    FunctionBodyMapper bodyStatementMapper;
    MapperRefactoringProcessor mapperRefactoringProcessor = new MapperRefactoringProcessor();


    final IContainer container1;
    final IContainer container2;

    protected final List<FunctionDeclaration> addedOperations = new ArrayList<>();
    protected final List<FunctionDeclaration> removedOperations = new ArrayList<>();

    public ContainerDiff(IContainer container1, IContainer container2) {
        this.container1 = container1;
        this.container2 = container2;
    }

    public IContainer getContainer1() {
        return container1;
    }

    public IContainer getContainer2() {
        return container2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        this.addedOperations.add(addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        this.removedOperations.add(removedOperation);
    }

    public List<FunctionDeclaration> getAddedOperations() {
        return addedOperations;
    }

    public List<FunctionDeclaration> getRemovedOperations() {
        return removedOperations;
    }

    public List<UMLOperationDiff> getOperationDiffList() {
        return operationDiffList;
    }

    public UMLOperationDiff getOperationDiff(FunctionDeclaration operation1, FunctionDeclaration operation2) {
        for (UMLOperationDiff diff : operationDiffList) {
            if (diff.function1.equals(operation1) && diff.function2.equals(operation2)) {
                return diff;
            }
        }
        return null;
    }

    public List<FunctionBodyMapper> getBodyMapperList() {
        return bodyMapperList;
    }

    public List<IRefactoring> getRefactoringsBeforePostProcessing() {
        return this.refactorings;
    }

    public Set<MethodInvocationReplacement> getConsistentMethodInvocationRenames() {
        return consistentMethodInvocationRenames;
    }

    public void setConsistentMethodInvocationRenames(Set<MethodInvocationReplacement> consistentMethodInvocationRenames) {
        this.consistentMethodInvocationRenames = consistentMethodInvocationRenames;
    }
}
