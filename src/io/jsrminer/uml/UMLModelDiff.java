package io.jsrminer.uml;

import io.jsrminer.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class UMLModelDiff {
    private List<Refactoring> refactorings = new ArrayList<>();

    public void addRefactoring(Refactoring ref) {
        refactorings.add(ref);
    }

    public List<Refactoring> getRefactorings() {
        return refactorings;
    }
}
