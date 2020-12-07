package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.uml.UMLModel;

import java.util.*;

public class UMLModelDiff extends Diff {
    private List<IRefactoring> refactorings = new ArrayList<>();

    public final UMLModel model1;
    public final UMLModel model2;

    public UMLModelDiff(UMLModel model1, UMLModel model2) {
        this.model1 = model1;
        this.model2 = model2;
    }


    public void addRefactoring(IRefactoring ref) {
        refactorings.add(ref);
    }

    public List<IRefactoring> getRefactorings() {
        return refactorings;
    }
}