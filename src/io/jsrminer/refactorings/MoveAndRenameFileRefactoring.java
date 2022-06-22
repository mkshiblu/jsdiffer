package io.jsrminer.refactorings;

import io.rminerx.core.api.ISourceFile;

public class MoveAndRenameFileRefactoring extends Refactoring {
    private final ISourceFile originalFile;
    private final ISourceFile renamedFile;

    public MoveAndRenameFileRefactoring(ISourceFile originalFile, ISourceFile renamedFile) {
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

    public ISourceFile getMovedFile() {
        return renamedFile;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.MOVE_RENAME_FILE;
    }

    @Override
    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        sb.append(originalFile.getFilepath());
        sb.append(" moved and renamed to ");
        sb.append(renamedFile.getFilepath());
        return sb.toString();
    }
}
