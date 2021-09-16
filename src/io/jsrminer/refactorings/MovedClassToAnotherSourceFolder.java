package io.jsrminer.refactorings;

import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.IClassDeclaration;

public class MovedClassToAnotherSourceFolder {
    //private String className;
    private IClassDeclaration originalClass;
    private IClassDeclaration movedClass;
    private String originalPath;
    private String movedPath;

    public MovedClassToAnotherSourceFolder(IClassDeclaration originalClass, IClassDeclaration movedClass,
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

    public IClassDeclaration getOriginalClass() {
        return originalClass;
    }

    public IClassDeclaration getMovedClass() {
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
}
