package io.jsrminer.util;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.*;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminerx.core.api.IContainer;

import java.util.List;

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

    public static String getHeader() {
        return "project\tcommitId\tRefactoringType\tNameBefore\tNameAfter\tLocationBefore\tLocationAfter\tRefactoring";
    }

    public static String generateDisplayStringForRefactorings(String project, String commitId, List<IRefactoring> refactorings, boolean printHeader) {
        final StringBuilder builder = new StringBuilder();

        if (printHeader) {
            builder.append(getHeader());
            builder.append(System.lineSeparator());
        }

        refactorings.forEach(r -> {
            builder.append(project);
            builder.append("\t");
            builder.append(commitId);
            builder.append("\t");
            RefactoringDisplayFormatter.format(r, builder);
            builder.append(System.lineSeparator());
        });

        return builder.toString();
    }

    public static StringBuilder format(IRefactoring refactoring, StringBuilder builder) {
        builder.append(refactoring.getRefactoringType().toString());
        builder.append("\t");
        var afterBeforeInfo = RefactoringDisplayFormatter.formatAsAfterBefore(refactoring);
        builder.append(afterBeforeInfo);
        builder.append("\t");
        builder.append(refactoring.toString().replace("\t", " "));
        return builder;
    }

    public static String formatAsAfterBefore(IRefactoring refactoring) {
        var afterBeforeInfo = getAfterBeforeInfo(refactoring);
        if (afterBeforeInfo != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(afterBeforeInfo.getLocalNameBefore());
            builder.append("\t");
            builder.append(afterBeforeInfo.getLocalNameAfter());
            builder.append("\t");
            builder.append(afterBeforeInfo.getLocationBefore());
            builder.append("\t");
            builder.append(afterBeforeInfo.getLocationAfter());

            return builder.toString();
        } else {
            return "\t\t\t\t";
        }
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
            case EXTRACT_OPERATION:
                var extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        extractOperationRefactoring.getSourceOperationBeforeExtraction().getName()
                        , extractOperationRefactoring.getExtractedOperation().getName()
                        , getLocationString(extractOperationRefactoring.getSourceOperationBeforeExtraction().getSourceLocation())
                        , getLocationString(extractOperationRefactoring.getExtractedOperation().getSourceLocation())
                );

                break;
            case MOVE_OPERATION:
                var moveOperation = (MoveOperationRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        moveOperation.getOriginalOperation().getName()
                        , moveOperation.getMovedOperation().getName()
                        , getLocationString(moveOperation.getOriginalOperation().getSourceLocation())
                        , getLocationString(moveOperation.getMovedOperation().getSourceLocation())
                );
                break;
            case ADD_PARAMETER:
            case RENAME_PARAMETER:
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return afterBeforeInfo;
    }

    static String getLocationString(SourceLocation location) {
        return location.getFilePath() + ":" + getLocationStartAndEndString(location);
    }

    static String getContainerStartAndEndString(IContainer container) {
        return getLocationStartAndEndString(container.getSourceLocation());
    }

    static String getLocationStartAndEndString(SourceLocation location) {
        return ":" + (location.start)
                + "-" + (location.end);
    }
}
