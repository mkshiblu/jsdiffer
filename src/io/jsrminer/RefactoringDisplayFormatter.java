package io.jsrminer;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.MoveFileRefactoring;
import io.jsrminer.refactorings.RenameFileRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import io.jsrminer.refactorings.RenameVariableRefactoring;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminerx.core.api.IContainer;

public class RefactoringDisplayFormatter {

    private interface AfterBeforeInfo {
        String getLocalNameBefore();

        String getLocalNameAfter();

        String getLocationBefore();

        String getLocationAfter();
    }

    public static String formatAsAfterBefore(IRefactoring refactoring) {
        StringBuilder builder = new StringBuilder();
        var afterBeforeInfo = getAfterBeforeInfo(refactoring);

        if (afterBeforeInfo != null) {
            builder.append(afterBeforeInfo.getLocationBefore());
            builder.append("\t");
            builder.append(afterBeforeInfo.getLocalNameBefore());
            builder.append("\t");
            builder.append(afterBeforeInfo.getLocationAfter());
            builder.append("\t");
            builder.append(afterBeforeInfo.getLocalNameAfter());
        }
        return builder.toString();
    }


    public static AfterBeforeInfo getAfterBeforeInfo(IRefactoring refactoring) {
        AfterBeforeInfo afterBeforeInfo = null;
        switch (refactoring.getRefactoringType()) {
            case MOVE_FILE:
                var moveRefactoring = (MoveFileRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo() {
                    @Override
                    public String getLocalNameBefore() {
                        return moveRefactoring.getOriginalFileName();
                    }

                    public String getLocalNameAfter() {
                        return moveRefactoring.getMovedFileName();
                    }

                    public String getLocationBefore() {
                        return moveRefactoring.getOriginalPath() + ":0-" + (moveRefactoring.getOriginalFile().getSourceLocation().end + 1);
                    }

                    public String getLocationAfter() {
                        return moveRefactoring.getMovedToPath() + ":0-" + (moveRefactoring.getMovedFile().getSourceLocation().end + 1);
                    }
                };
                break;
            case RENAME_METHOD:
                var renameFunctionRefactoring = (RenameOperationRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo() {
                    @Override
                    public String getLocalNameBefore() {
                        return renameFunctionRefactoring.getOriginalOperation().getName();
                    }

                    public String getLocalNameAfter() {
                        return renameFunctionRefactoring.getRenamedOperation().getName();
                    }

                    public String getLocationBefore() {
                        return renameFunctionRefactoring.getOriginalOperation().getSourceLocation().getFilePath()
                                + getContainerStartAndEndString(renameFunctionRefactoring.getOriginalOperation());
                    }

                    public String getLocationAfter() {
                        return renameFunctionRefactoring.getRenamedOperation().getSourceLocation().getFilePath()
                                + getContainerStartAndEndString(renameFunctionRefactoring.getRenamedOperation());
                    }
                };
                break;
            case RENAME_VARIABLE:
                var renameVariableRefactoring = (RenameVariableRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo() {
                    @Override
                    public String getLocalNameBefore() {
                        return renameVariableRefactoring.getOriginalVariable().variableName;
                    }

                    public String getLocalNameAfter() {
                        return renameVariableRefactoring.getRenamedVariable().variableName;
                    }

                    public String getLocationBefore() {
                        return renameVariableRefactoring.getOriginalVariable().getSourceLocation().getFilePath()
                                + getLocationStartAndEndString(renameVariableRefactoring.getOriginalVariable().getSourceLocation());
                    }

                    public String getLocationAfter() {
                        return renameVariableRefactoring.getRenamedVariable().getSourceLocation().getFilePath()
                                + getLocationStartAndEndString(renameVariableRefactoring.getRenamedVariable().getSourceLocation());
                    }
                };
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return afterBeforeInfo;
    }

    static String getContainerStartAndEndString(IContainer container) {
        return getLocationStartAndEndString(container.getSourceLocation());
    }

    static String getLocationStartAndEndString(SourceLocation location) {
        return ":" + (location.start)
                + "-" + (location.end);
    }
}
