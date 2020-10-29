package io.jsrminer.refactorings;

public interface IRefactoring {
    public String getName();

    public RefactoringType getRefactoringType();
}
