package io.jsrminer.uml.diff;

import io.jsrminer.Refactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLModel;

import java.util.*;

public class UMLModelDiff extends Diff {
    private List<Refactoring> refactorings = new ArrayList<>();

    public final UMLModel model1;
    public final UMLModel model2;

    private final Set<FunctionDeclaration> addedOperations = new HashSet<>();
    private final Set<FunctionDeclaration> removedOperations = new HashSet<>();

    public UMLModelDiff(UMLModel model1, UMLModel model2) {
        this.model1 = model1;
        this.model2 = model2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        addedOperations.add(addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        removedOperations.add(removedOperation);
    }

    public void addRefactoring(Refactoring ref) {
        refactorings.add(ref);
    }

    public List<Refactoring> getRefactorings() {
        return refactorings;
    }
}