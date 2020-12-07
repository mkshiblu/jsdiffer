package io.jsrminer.api;

import io.jsrminer.refactorings.RefactoringType;

public interface IRefactoring {
    public String getName();

    public RefactoringType getRefactoringType();
}
