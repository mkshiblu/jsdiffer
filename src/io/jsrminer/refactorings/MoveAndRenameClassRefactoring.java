package io.jsrminer.refactorings;

import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.IClassDeclaration;

public class MoveAndRenameClassRefactoring extends Refactoring {
    private IClassDeclaration originalClass;
    private IClassDeclaration renamedClass;

    public MoveAndRenameClassRefactoring(IClassDeclaration originalClass,  IClassDeclaration renamedClass) {
        this.originalClass = originalClass;
        this.renamedClass = renamedClass;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalClass.getName());
        sb.append(" moved and renamed to ");
        sb.append(renamedClass.getName());
        return sb.toString();
    }

    public RenamePattern getRenamePattern() {
        int separatorPos = PrefixSuffixUtils.separatorPosOfCommonSuffix('.'
                , originalClass.getParentContainerQualifiedName(), renamedClass.getParentContainerQualifiedName());
        if (separatorPos == -1) {
            return new RenamePattern(originalClass.getParentContainerQualifiedName(), renamedClass.getParentContainerQualifiedName());
        }
        String originalPath = originalClass.getName().substring(0, originalClass.getName().length() - separatorPos);
        String movedPath = renamedClass.getName().substring(0, renamedClass.getName().length() - separatorPos);
        return new RenamePattern(originalPath, movedPath);
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        return RefactoringType.MOVE_RENAME_CLASS;
    }

    public String getOriginalClassName() {
        return originalClass.getName();
    }

    public String getRenamedClassName() {
        return renamedClass.getName();
    }

    public String getMovedClassName() {
        return getRenamedClassName();
    }

    public IClassDeclaration getOriginalClass() {
        return originalClass;
    }

    public IClassDeclaration getRenamedClass() {
        return renamedClass;
    }

    public IClassDeclaration getMovedClass() {
        return getRenamedClass();
    }
}
