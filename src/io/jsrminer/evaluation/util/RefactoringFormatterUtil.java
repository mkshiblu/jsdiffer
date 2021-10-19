package io.jsrminer.evaluation.util;

import io.jsrminer.evaluation.Ref;
import io.jsrminer.evaluation.ValidationType;

import java.util.Comparator;
import java.util.List;


public class RefactoringFormatterUtil {

    static final String delimiter = "\t";

    private String[] tableHeaders = new String[]{"Project",
            "CommitId",
            "RefactoringType",
            "NameBefore",
            "NameAfter",
            "LocationBefore",
            "LocationAfter",
            "Refactoring",
            "Validation",
            "FoundByRd",
            "Comment",
    };

    public String formatAsCsv(List<Ref> refactorings) {
        StringBuilder builder = new StringBuilder(256000);
        writeHeader(builder);
        builder.append(System.lineSeparator());

        var sortedStream = refactorings.stream()
                .sorted(Comparator.comparing(Ref::getRepository)
                        .thenComparing(ref -> ref.getCommit())
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
        builder.append(ref.getRepository());
        builder.append(delimiter);
        builder.append(ref.getCommit());
        builder.append(delimiter);
        builder.append(ref.getRefType());
        builder.append(delimiter);
        builder.append(ref.getLocalNameBefore());
        builder.append(delimiter);
        builder.append(ref.getLocalNameAfter());
        builder.append(delimiter);
        builder.append(ref.getLocationBefore());
        builder.append(delimiter);
        builder.append(ref.getLocationAfter());
        builder.append(delimiter);
        builder.append(delimiter);
        builder.append(isTp ? "TP" : "");
        builder.append(delimiter);
        builder.append(isTp ? "Y" : "");
        builder.append(delimiter);
    }
}
