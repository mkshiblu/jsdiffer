package io.jsrminer.refactorings;

import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.ISourceFile;

public class MoveFileToAnotherSourceFolderRefactoring extends Refactoring {
    //private String className;
    private ISourceFile originalClass;
    private ISourceFile movedClass;
    private String originalPath;
    private String movedPath;

    public MoveFileToAnotherSourceFolderRefactoring(ISourceFile originalClass, ISourceFile movedClass,
                                                    String originalPath, String movedPath) {
        this.originalClass = originalClass;
        this.movedClass = movedClass;
        this.originalPath = originalPath;
        this.movedPath = movedPath;
    }

    public String getOriginalClassName() {
        return originalClass.getName();
    }

    public String getMovedClassName() {
        return movedClass.getName();
    }

    public ISourceFile getOriginalClass() {
        return originalClass;
    }

    public ISourceFile getMovedClass() {
        return movedClass;
    }

    public RenamePattern getRenamePattern() {
        int separatorPos = PrefixSuffixUtils.separatorPosOfCommonSuffix('/', originalPath, movedPath);
        if (separatorPos == -1) {
            return new RenamePattern(originalPath, movedPath);
        }
        String original = originalPath.substring(0, originalPath.length() - separatorPos);
        String moved = movedPath.substring(0, movedPath.length() - separatorPos);
        return new RenamePattern(original, moved);
    }

    @Override
    public RefactoringType getRefactoringType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
