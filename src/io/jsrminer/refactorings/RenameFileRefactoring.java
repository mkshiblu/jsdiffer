package io.jsrminer.refactorings;

import io.rminerx.core.api.ISourceFile;

public class RenameFileRefactoring extends Refactoring {
    private final ISourceFile originalFile;
    private final ISourceFile renamedFile;

    public RenameFileRefactoring(ISourceFile originalFile, ISourceFile renamedFile) {
        this.originalFile = originalFile;
        this.renamedFile = renamedFile;
    }

    public String getRenamedFileName() {
        return renamedFile.getName();
    }

    public String getOriginalFileName() {
        return originalFile.getName();
    }

    public ISourceFile getOriginalFile() {
        return originalFile;
    }

    public ISourceFile getRenamedFile() {
        return renamedFile;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.RENAME_FILE;
    }

    @Override
    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalFile.getFilepath());
        sb.append(" renamed to ");
        sb.append(renamedFile.getFilepath());
        return sb.toString();
    }
}
