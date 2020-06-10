package io.jsrminer.uml;

import java.util.ArrayList;
import java.util.List;

public class UMLModelDiff {
    private List<String> refactorings = new ArrayList<>();

    public void addRefactoring(String ref) {
        refactorings.add(ref);
    }

    public List<String> getRefactorings() {
        return refactorings;
    }
}
