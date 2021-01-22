package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.MethodInvocationReplacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import io.rminer.core.api.ISourceFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a diff between two containers
 */
public class SourceFileDiff {
    public final ISourceFile source1;
    public final ISourceFile source2;

    private final List<IRefactoring> refactorings = new ArrayList<>();
    private Set<MethodInvocationReplacement> consistentMethodInvocationRenames;

    //    private Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap = new LinkedHashMap<>();
//    private Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap = new LinkedHashMap<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>>();
//    private Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap = new LinkedHashMap<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>>();
//
    private final List<FunctionBodyMapper> bodyMapperList = new ArrayList<>();
    private List<UMLOperationDiff> operationDiffList = new ArrayList<>();

    /**
     * Name map
     */
    private final List<FunctionDeclaration> addedOperations = new ArrayList<>();
    private final List<FunctionDeclaration> removedOperations = new ArrayList<>();

    public SourceFileDiff(ISourceFile source1, ISourceFile source2) {
        this.source1 = source1;
        this.source2 = source2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        addedOperations.add(addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        removedOperations.add(removedOperation);
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

    public List<IRefactoring> getRefactorings() {
        return refactorings;
    }

    public Set<MethodInvocationReplacement> getConsistentMethodInvocationRenames() {
        return consistentMethodInvocationRenames;
    }

    public void setConsistentMethodInvocationRenames(Set<MethodInvocationReplacement> consistentMethodInvocationRenames) {
        this.consistentMethodInvocationRenames = consistentMethodInvocationRenames;
    }

    public static boolean allMappingsAreExactMatches(FunctionBodyMapper operationBodyMapper) {
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        int tryMappings = 0;
        int mappingsWithTypeReplacement = 0;
        for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
            if (mapping.getFragment1().getText().equals("try") && mapping.getFragment2()
                    .getText().equals("try")) {
                tryMappings++;
            }
            if (mapping.containsReplacement(ReplacementType.TYPE)) {
                mappingsWithTypeReplacement++;
            }
        }
        if (mappings == operationBodyMapper.getExactMatches().size() + tryMappings) {
            return true;
        }
        if (mappings == operationBodyMapper.getExactMatches().size() + tryMappings + mappingsWithTypeReplacement && mappings > mappingsWithTypeReplacement) {
            return true;
        }
        return false;
    }
}
