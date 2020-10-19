package io.jsrminer.refactorings;

import org.eclipse.jgit.annotations.NonNull;

public class Refactoring implements IRefactoring {

    protected RefactoringType refactoringType;

    public Refactoring(@NonNull RefactoringType refactoringType) {
        this.refactoringType = refactoringType;
    }

    @Override
    public String getName() {
        return this.refactoringType.getTitle();
    }

    @Override
    public RefactoringType getRefactoringType() {
        return refactoringType;
    }

    @Override
    public String toString() {
        return refactoringType.toString();
    }
}
