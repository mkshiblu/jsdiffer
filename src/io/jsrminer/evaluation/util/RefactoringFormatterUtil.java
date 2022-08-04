package io.jsrminer.evaluation.util;

import io.jsrminer.evaluation.Ref;
import io.jsrminer.evaluation.ValidationType;
import io.jsrminer.util.RefactoringDisplayFormatter;

import java.util.Comparator;
import java.util.List;


public class RefactoringFormatterUtil {

    static final String delimiter = "\t";

    private String[] tableHeaders = new String[]{"project",
            "commit_id",
            "refactoring_type",
            "name_before",
            "name_after",
            "location_before",
            "location_after",
            "refactoring",
            "validation",
            "rd_validation",
            "Comment",
    };

    public String formatAsCsv(List<Ref> refactorings) {
        StringBuilder builder = new StringBuilder(256000);
        writeHeader(builder);
        builder.append(System.lineSeparator());

        var sortedStream = refactorings.stream()
                .sorted(Comparator.comparing(Ref::getProject)
                        .thenComparing(ref -> ref.getCommitId())
                        .thenComparing(ref -> ref.getRefType().toString().toLowerCase()));
        sortedStream.forEachOrdered(ref -> {
            writeRow(ref, builder);
            builder.append(System.lineSeparator());
        });

        return builder.toString();
    }

    private void writeHeader(StringBuilder builder) {
        builder.append(String.join(delimiter, tableHeaders));
    }

    private void writeRow(Ref ref, StringBuilder builder) {
        boolean isTp = ref.getValidationType() == ValidationType.TruePositive;
        builder.append(ref.getProject());
        builder.append(delimiter);
        builder.append(ref.getCommitId());
        builder.append(delimiter);
        builder.append(ref.getRefType());
        builder.append(delimiter);
        builder.append(ref.getLocalNameBefore());
        builder.append(delimiter);
        builder.append(ref.getLocalNameAfter());
        builder.append(delimiter);
        builder.append(RefactoringDisplayFormatter.getLocationString(ref.getLocationBefore()));
        builder.append(delimiter);
        builder.append(RefactoringDisplayFormatter.getLocationString(ref.getLocationAfter()));
        builder.append(delimiter);
        builder.append(delimiter);
        builder.append(isTp ? "TP" : "");
        builder.append(delimiter);
        builder.append(isTp ? "Y" : "");
        builder.append(delimiter);
    }
}
