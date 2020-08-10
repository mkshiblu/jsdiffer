package io.jsrminer.uml.diff;

import io.jsrminer.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class UMLModelDiff extends Diff {
    private List<Refactoring> refactorings = new ArrayList<>();

    public void addRefactoring(Refactoring ref) {
        refactorings.add(ref);
    }

    public List<Refactoring> getRefactorings() {
        return refactorings;
    }
}