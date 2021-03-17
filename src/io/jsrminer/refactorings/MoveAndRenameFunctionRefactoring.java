package io.jsrminer.refactorings;

import io.rminerx.core.api.IFunctionDeclaration;

public class MoveAndRenameFunctionRefactoring extends Refactoring{
    private final IFunctionDeclaration originalFunction;
    private final IFunctionDeclaration renamedFunction;

    public MoveAndRenameFunctionRefactoring(IFunctionDeclaration originalFunction, IFunctionDeclaration renamedFunction) {
        this.originalFunction = originalFunction;
        this.renamedFunction = renamedFunction;
    }

    public String getRenamedFunctionName() {
        return renamedFunction.getName();
    }

    public String getOriginalFunctionName() {
        return originalFunction.getName();
    }

    public IFunctionDeclaration getOriginalFunction() {
        return originalFunction;
    }

    public IFunctionDeclaration getMovedFile() {
        return renamedFunction;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.MOVE_AND_RENAME_OPERATION;
    }

    @Override
    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalFunction.getSourceLocation().getFilePath());
        sb.append(" moved and renamed to ");
        sb.append(renamedFunction.getSourceLocation().getFilePath());
        return sb.toString();
    }
}
