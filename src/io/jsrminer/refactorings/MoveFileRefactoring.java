package io.jsrminer.refactorings;

import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.ISourceFile;

public class MoveFileRefactoring extends Refactoring {
    //private String className;
    private ISourceFile originalClass;
    private ISourceFile movedClass;
    private String originalPath;
    private String movedPath;

    public MoveFileRefactoring(ISourceFile originalClass, ISourceFile movedClass) {
        this.originalClass = originalClass;
        this.movedClass = movedClass;
        this.originalPath = originalClass.getFilepath();
        this.movedPath = movedClass.getFilepath();
    }

    public String getOriginalPath() {
        return this.originalPath;
    }

    public String getMovedFileName() {
        return movedClass.getName();
    }

    public String getOriginalFileName() {
        return originalClass.getName();
    }

    public String getOriginalPathDirectory() {
        return originalClass.getDirectoryPath();
    }

    public String getMovedToPath() {
        return this.movedPath;
    }

    public String getMovedPathDirectory() {
        return movedClass.getDirectoryPath();
    }

    public ISourceFile getOriginalFile() {
        return originalClass;
    }

    public ISourceFile getMovedFile() {
        return movedClass;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.MOVE_FILE;
    }

    @Override
    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        String originalPath = getOriginalPath();//pattern.getBefore().endsWith("/") ? pattern.getBefore().substring(0, pattern.getBefore().length() - 1) : pattern.getBefore();
        sb.append(originalPath);
        sb.append(" to ");
        String movedPath = getMovedToPath();//pattern.getAfter().endsWith("/") ? pattern.getAfter().substring(0, pattern.getAfter().length() - 1) : pattern.getAfter();
        sb.append(movedPath);
        return sb.toString();
    }
}
