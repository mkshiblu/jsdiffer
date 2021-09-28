package io.jsrminer.evaluation;

import java.util.Arrays;

class CommitRefactoringsDiffFormatter {

    private CommitRefactoringsDiff refactoringsDiff;

    public CommitRefactoringsDiffFormatter(CommitRefactoringsDiff refactoringsDiff){
        this.refactoringsDiff = refactoringsDiff;
    }

    public String formatAsTable(){
        StringBuilder builder = new StringBuilder();
        builder.append(refactoringsDiff.getCommitId());
        builder.append("\t");

        if (refactoringsDiff.getCompletelyUnmatchedTypeRefsInRd().size() > 0) {
            builder.append("Missed Refactorings Type in RD: ");
            builder.append(refactoringsDiff.getCompletelyUnmatchedTypeRefsInRd().keySet().toString());
        }
//
        builder.append(System.lineSeparator());
        builder.append(refactoringsDiff.getUnmatchedInRdCount() + " Unmatched in Rd-lines: ");
        var missedLines = refactoringsDiff.getAllUnmatchedInRds().stream().map(rdRow -> rdRow.lineNo).toArray(Integer[]::new);
        builder.append(Arrays.toString(missedLines));

        builder.append(System.lineSeparator());
        builder.append(refactoringsDiff.getTotalMatchedCount() + " Matched-lines: ");
        var matchedLines = refactoringsDiff.getAllMatched().stream().map(rdRow -> rdRow.lineNo).toArray(Integer[]::new);
        builder.append(Arrays.toString(matchedLines));

        return builder.toString();
    }

    public String formatAsJson(){
        return  "";
    }
}
