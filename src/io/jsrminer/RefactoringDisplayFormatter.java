package io.jsrminer;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.MoveFileRefactoring;

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

            default:
                break;
        }
        return afterBeforeInfo;
    }
}
