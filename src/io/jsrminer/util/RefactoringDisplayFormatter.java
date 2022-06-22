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
        return "project\tcommit_id\trefactoring_type\tname_before\tname_after\tlocation_before\tlocation_after\tdescription";
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
        builder.append(refactoring.getRefactoringType().toString().toUpperCase().replace("OPERATION", "FUNCTION").replace("METHOD", "FUNCTION"));
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
            return "\t\t\t";
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
                        , getLocationString(moveRefactoring.getOriginalFile().getSourceLocation())
                        , getLocationString(moveRefactoring.getMovedFile().getSourceLocation())
                );
                break;
            case RENAME_METHOD:
                var renameFunctionRefactoring = (RenameOperationRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        renameFunctionRefactoring.getOriginalOperation().getName()
                        , renameFunctionRefactoring.getRenamedOperation().getName()
                        , getLocationString(renameFunctionRefactoring.getOriginalOperation().getSourceLocation())
                        , getLocationString(renameFunctionRefactoring.getRenamedOperation().getSourceLocation())
                );
                break;
            case PARAMETERIZE_VARIABLE:
            case RENAME_VARIABLE:
            case RENAME_PARAMETER:
                var renameVariableRefactoring = (RenameVariableRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        renameVariableRefactoring.getOriginalVariable().variableName
                        , renameVariableRefactoring.getRenamedVariable().variableName
                        , getLocationString(renameVariableRefactoring.getOriginalVariable().getSourceLocation())
                        , getLocationString(renameVariableRefactoring.getRenamedVariable().getSourceLocation())
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
            case INLINE_OPERATION:
                var inlineOperationRefactoring = (InlineOperationRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        inlineOperationRefactoring.getInlinedOperation().getName()
                        , inlineOperationRefactoring.getTargetOperationAfterInline().getName()
                        , getLocationString(inlineOperationRefactoring.getInlinedOperation().getSourceLocation())
                        , getLocationString(inlineOperationRefactoring.getTargetOperationAfterInline().getSourceLocation())
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
            case RENAME_FILE:
                var renameFile = (RenameFileRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        renameFile.getOriginalFileName()
                        , renameFile.getRenamedFileName()
                        , getLocationString(renameFile.getOriginalFile().getSourceLocation())
                        , getLocationString(renameFile.getRenamedFile().getSourceLocation())
                );
                break;
            case ADD_PARAMETER:
                var addParameterRefactoring = (AddParameterRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        ""
                        , addParameterRefactoring.getParameter().name
                        , ""
                        , getLocationString(addParameterRefactoring.getParameter().getSourceLocation()));
                break;
            case REMOVE_PARAMETER:
                var removeParameterRefactoring = (RemoveParameterRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        removeParameterRefactoring.getParameter().name
                        , ""
                        , getLocationString(removeParameterRefactoring.getParameter().getSourceLocation())
                        , "");
                break;
            case RENAME_CLASS:
                var renameClassRefactoring = (RenameClassRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        renameClassRefactoring.getOriginalClass().getName()
                        , renameClassRefactoring.getRenamedClass().getName()
                        , getLocationString(renameClassRefactoring.getOriginalClass().getSourceLocation())
                        , getLocationString(renameClassRefactoring.getRenamedClass().getSourceLocation())
                );
                break;

            case MOVE_CLASS:
                var moveClassRefactoring = (MoveClassRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        moveClassRefactoring.getOriginalClassName()
                        , moveClassRefactoring.getMovedClassName()
                        , getLocationString(moveClassRefactoring.getOriginalClass().getSourceLocation())
                        , getLocationString(moveClassRefactoring.getMovedClass().getSourceLocation())
                );
                break;
            case MOVE_RENAME_FILE:
                var moveAndRenameFileRefactoring = (MoveAndRenameFileRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        moveAndRenameFileRefactoring.getOriginalFileName()
                        , moveAndRenameFileRefactoring.getRenamedFileName()
                        , getLocationString(moveAndRenameFileRefactoring.getOriginalFile().getSourceLocation())
                        , getLocationString(moveAndRenameFileRefactoring.getMovedFile().getSourceLocation())
                );
                break;
            case MOVE_RENAME_CLASS:
                var moveAndRenameClassRefactoring = (MoveAndRenameClassRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        moveAndRenameClassRefactoring.getOriginalClassName()
                        , moveAndRenameClassRefactoring.getRenamedClassName()
                        , getLocationString(moveAndRenameClassRefactoring.getOriginalClass().getSourceLocation())
                        , getLocationString(moveAndRenameClassRefactoring.getRenamedClass().getSourceLocation())
                );
                break;
            case CHANGE_VARIABLE_KIND:
                var changeVariableKindRefactoring = (ChangeVariableKindRefactoring) refactoring;
                afterBeforeInfo = new AfterBeforeInfo(
                        changeVariableKindRefactoring.getOriginalVariable().getKind().keywordName
                        , changeVariableKindRefactoring.getChangedTypeVariable().getKind().keywordName
                        , getLocationString(changeVariableKindRefactoring.getOriginalVariable().getSourceLocation())
                        , getLocationString(changeVariableKindRefactoring.getChangedTypeVariable().getSourceLocation())
                );
                break;
            default:
                throw new UnsupportedOperationException(refactoring.getRefactoringType().toString());
        }
        return afterBeforeInfo;
    }

    public static String getLocationString(SourceLocation location) {
        return location.getFilePath() + ":" + getLocationStartAndEndString(location);
    }

    static String getContainerStartAndEndString(IContainer container) {
        return getLocationStartAndEndString(container.getSourceLocation());
    }

    static String getLocationStartAndEndString(SourceLocation location) {
        return String.format("%d-%d|(%d,%d)-(%d,%d)"
                , location.start
                , location.end
                , location.startLine
                , location.startColumn
                , location.endLine
                , location.endColumn);
    }
}
