package io.jsrminer.evaluation;

import java.util.ArrayList;
import java.util.List;

public class RefactoringValidator {
    private final List<Ref> refactorings;

    public RefactoringValidator(List<Ref> refactorings) {
        this.refactorings = refactorings;
    }

    public List<Ref> validateTruePositives(List<Ref> reportedRefactoringsByOtherToolsOrDocumentation) {
        List<Ref> truePositives = new ArrayList<>();
        for (var refactoring : refactorings) {
            for (var reportedRefactoring : reportedRefactoringsByOtherToolsOrDocumentation) {
                if (RefactoringMatcher.isMatch(reportedRefactoring, refactoring)) {
                    refactoring.setValidationType(ValidationType.TruePositive);
                    truePositives.add(refactoring);
                    break;
                }
            }
        }
        return truePositives;
    }
}
