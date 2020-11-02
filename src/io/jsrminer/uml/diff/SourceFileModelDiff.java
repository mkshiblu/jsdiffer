package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.IRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a diff between two version of the same source file
 */
public class SourceFileModelDiff {
    public final SourceFileModel sourceFileModel1;
    public final SourceFileModel sourceFileModel2;

    public final List<IRefactoring> refactorings = new ArrayList<>();

    /**
     * Name map
     */
    private final Map<String, FunctionDeclaration> addedOperations = new LinkedHashMap<>();
    private final Map<String, FunctionDeclaration> removedOperations = new LinkedHashMap<>();

    public SourceFileModelDiff(SourceFileModel sourceFileModel1, SourceFileModel sourceFileModel2) {
        this.sourceFileModel1 = sourceFileModel1;
        this.sourceFileModel2 = sourceFileModel2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        if (addedOperations.containsKey(addedOperation.name))
            throw new RuntimeException("Duplicate names for removed operations" + addedOperation);
        addedOperations.put(addedOperation.name, addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        if (removedOperations.containsKey(removedOperation.name))
            throw new RuntimeException("Duplicate names for removed operations" + removedOperation);
        removedOperations.put(removedOperation.name, removedOperation);
    }

    public boolean isRemovedOperation(String functionName) {
        return this.removedOperations.containsKey(functionName);
    }

    public Map<String, FunctionDeclaration> getAddedOperations() {
        return addedOperations;
    }

    public Map<String, FunctionDeclaration> getRemovedOperations() {
        return removedOperations;
    }
}
