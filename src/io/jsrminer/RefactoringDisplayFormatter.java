package io.jsrminer;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.*;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminerx.core.api.IContainer;

public class RefactoringDisplayFormatter {
    private static class AfterBeforeInfo {
        private final String nameBefore;
        private final String nameAfter;
        private final String locationBefore;
        private final String locationAfter;

        AfterBeforeInfo(String nameBefore, String nameAfter, String locationBefore, String locationAfter) {
            this.nameBefore = nameBefore;
            this.nameAfter = nameAfter;
            this.locationBefore = locationBefore;
            this.locationAfter = locationAfter;
        }

        public String getLocalNameBefore() {
            return nameBefore;
        }

        public String getLocalNameAfter() {
            return nameAfter;
        }

        public String getLocationBefore() {
            return locationBefore;
        }

        public String getLocationAfter() {
            return locationAfter;
        }
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
                afterBeforeInfo = new AfterBeforeInfo(
                        moveRefactoring.getOriginalFileName()
                        , moveRefactoring.getMovedFileName()
                        , (moveRefactoring.getOriginalPath() + ":0-" + (moveRefactoring.getOriginalFile().getSourceLocation().end + 1))
                        , moveRefactoring.getMovedToPath() + ":0-" + (moveRefactoring.getMovedFile().getSourceLocation().end + 1)
                );
                break;
            case RENAME_METHOD:
                var renameFunctionRefactoring = (RenameOperationRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        renameFunctionRefactoring.getOriginalOperation().getName()
                        , renameFunctionRefactoring.getRenamedOperation().getName()
                        , renameFunctionRefactoring.getOriginalOperation().getSourceLocation().getFilePath()
                        + getContainerStartAndEndString(renameFunctionRefactoring.getOriginalOperation())
                        , renameFunctionRefactoring.getRenamedOperation().getSourceLocation().getFilePath()
                        + getContainerStartAndEndString(renameFunctionRefactoring.getRenamedOperation())
                );
                break;
            case RENAME_VARIABLE:
                var renameVariableRefactoring = (RenameVariableRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        renameVariableRefactoring.getOriginalVariable().variableName
                        , renameVariableRefactoring.getRenamedVariable().variableName
                        , renameVariableRefactoring.getOriginalVariable().getSourceLocation().getFilePath()
                        + getLocationStartAndEndString(renameVariableRefactoring.getOriginalVariable().getSourceLocation())
                        , renameVariableRefactoring.getRenamedVariable().getSourceLocation().getFilePath()
                        + getLocationStartAndEndString(renameVariableRefactoring.getRenamedVariable().getSourceLocation())
                );
                break;
            case ADD_PARAMETER:
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
