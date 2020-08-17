package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a diff between two version of the same source file
 */
public class SourceFileModelDiff {
    public final SourceFileModel sourceFileModel1;
    public final SourceFileModel sourceFileModel2;

    private final Set<FunctionDeclaration> addedOperations = new HashSet<>();
    private final Set<FunctionDeclaration> removedOperations = new HashSet<>();

    public SourceFileModelDiff(SourceFileModel sourceFileModel1, SourceFileModel sourceFileModel2) {
        this.sourceFileModel1 = sourceFileModel1;
        this.sourceFileModel2 = sourceFileModel2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        addedOperations.add(addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        removedOperations.add(removedOperation);
    }
}
