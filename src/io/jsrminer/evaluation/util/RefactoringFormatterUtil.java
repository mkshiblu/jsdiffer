package io.jsrminer.evaluation.util;

import io.jsrminer.evaluation.Ref;
import io.jsrminer.evaluation.RmRow;
import io.jsrminer.evaluation.ValidationType;

import java.util.Comparator;
import java.util.List;


public class RefactoringFormatterUtil {

    String delimeter = "\t";

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
        builder.append(String.join(delimeter, tableHeaders));
    }

    private void writeRow(Ref ref, StringBuilder builder) {
        boolean isTp = ref.getValidationType() == ValidationType.TruePositive;
        builder.append(ref.getRepository());
        builder.append(delimeter);
        builder.append(ref.getCommit());
        builder.append(delimeter);
        builder.append(ref.getRefType());
        builder.append(delimeter);
        builder.append(ref.getLocalNameBefore());
        builder.append(delimeter);
        builder.append(ref.getLocalNameAfter());
        builder.append(delimeter);
        builder.append(ref.getLocationBefore());
        builder.append(delimeter);
        builder.append(ref.getLocationAfter());
        builder.append(delimeter);
        builder.append(delimeter);
        builder.append(isTp ? "TP" : "");
        builder.append(delimeter);
        builder.append(isTp ? "Y" : "");
        builder.append(delimeter);
    }
}
