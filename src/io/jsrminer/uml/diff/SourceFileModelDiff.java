package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a diff between two version of the same source file
 */
public class SourceFileModelDiff {
    public final SourceFileModel sourceFileModel1;
    public final SourceFileModel sourceFileModel2;

    private final Map<String, FunctionDeclaration> addedOperations = new HashMap<>();
    private final Map<String, FunctionDeclaration> removedOperations = new HashMap<>();

    public SourceFileModelDiff(SourceFileModel sourceFileModel1, SourceFileModel sourceFileModel2) {
        this.sourceFileModel1 = sourceFileModel1;
        this.sourceFileModel2 = sourceFileModel2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        addedOperations.put(addedOperation.name, addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        removedOperations.put(removedOperation.name, removedOperation);
    }

    public boolean isRemovedOperation(String functionName) {
        return this.removedOperations.containsKey(functionName);
    }
}
