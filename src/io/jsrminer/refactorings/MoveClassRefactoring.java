package io.jsrminer.refactorings;


import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.IClassDeclaration;

public class MoveClassRefactoring extends Refactoring {
    private IClassDeclaration originalClass;
    private IClassDeclaration movedClass;

    public MoveClassRefactoring(IClassDeclaration originalClass, IClassDeclaration movedClass) {
        this.originalClass = originalClass;
        this.movedClass = movedClass;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalClass.getSourceLocation().getFilePath() + "|" + originalClass.getQualifiedName());
        sb.append(" moved to ");
        sb.append(movedClass.getSourceLocation().getFilePath() + "|" + movedClass.getQualifiedName());
        return sb.toString();
    }

    public RenamePattern getRenamePattern() {
        int separatorPos = PrefixSuffixUtils.separatorPosOfCommonSuffix('.', originalClass.getName(), movedClass.getName());
        if (separatorPos == -1) {
            return new RenamePattern(originalClass.getName(), movedClass.getName());
        }
        String originalPath = originalClass.getName().substring(0, originalClass.getName().length() - separatorPos);
        String movedPath = movedClass.getName().substring(0, movedClass.getName().length() - separatorPos);
        return new RenamePattern(originalPath, movedPath);
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        return RefactoringType.MOVE_CLASS;
    }

    public String getOriginalClassName() {
        return originalClass.getName();
    }

    public String getMovedClassName() {
        return movedClass.getName();
    }

    public IClassDeclaration getOriginalClass() {
        return originalClass;
    }

    public IClassDeclaration getMovedClass() {
        return movedClass;
    }
}
